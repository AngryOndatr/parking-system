package com.parking.api_gateway.security.service;

import com.parking.api_gateway.security.entity.UserSecurityEntity;
import com.parking.api_gateway.security.repository.UserSecurityRepository;
import com.parking.api_gateway.security.dto.AuthRequest;
import com.parking.api_gateway.security.dto.AuthResponse;
import com.parking.api_gateway.security.exception.AccountLockedException;
import com.parking.api_gateway.security.exception.CredentialsExpiredException;
import com.parking.api_gateway.security.exception.InvalidCredentialsException;
import com.parking.api_gateway.security.exception.AccountDisabledException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserSecurityService {
    
    private final UserSecurityRepository userRepository;
    private final SecurityAuditService auditService;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12); // High strength
    
    // Security Configuration
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCKOUT_DURATION_MINUTES = 30;
    private static final int PASSWORD_EXPIRY_DAYS = 90;
    private static final int SESSION_TIMEOUT_HOURS = 8;
    
    /**
     * Authenticate user with comprehensive security checks
     */
    public Mono<AuthResponse> authenticateUser(AuthRequest request, String clientIpAddress, String userAgent) {
        return Mono.fromCallable(() -> {
            // 1. Find user
            UserSecurityEntity user = findActiveUser(request.getUsername())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid username or password"));
            
            // 2. Pre-authentication security checks
            validateAccountStatus(user);
            
            // 3. Password verification
            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                handleFailedLogin(user, clientIpAddress, "Invalid password");
                throw new InvalidCredentialsException("Invalid username or password");
            }
            
            // 4. Post-authentication checks
            checkPasswordExpiry(user);
            checkCredentialsExpiry(user);
            
            // 5. Record successful login
            recordSuccessfulLogin(user, clientIpAddress, userAgent);
            
            // 6. Create auth response
            return createAuthResponse(user, clientIpAddress, userAgent);
            
        }).subscribeOn(Schedulers.boundedElastic())
          .doOnSuccess(response -> auditService.logSuccessfulLogin(response.getUser().getUsername(), clientIpAddress, userAgent))
          .doOnError(error -> auditService.logFailedLogin(request.getUsername(), clientIpAddress, userAgent, error.getMessage()));
    }
    
    /**
     * Find active user with all security checks
     */
    private Optional<UserSecurityEntity> findActiveUser(String login) {
        return userRepository.findActiveUserByLogin(login, LocalDateTime.now());
    }
    
    /**
     * Validate account status before authentication
     */
    private void validateAccountStatus(UserSecurityEntity user) {
        if (!user.getEnabled()) {
            throw new AccountDisabledException("Account is disabled");
        }
        
        if (!user.getAccountNonExpired()) {
            throw new AccountDisabledException("Account has expired");
        }
        
        if (!user.isAccountNonLocked()) {
            throw new AccountLockedException("Account is locked due to security reasons");
        }
        
        // Check for temporary lockout
        if (user.getAccountLockedUntil() != null && 
            LocalDateTime.now().isBefore(user.getAccountLockedUntil())) {
            long minutesLeft = java.time.Duration.between(LocalDateTime.now(), user.getAccountLockedUntil()).toMinutes();
            throw new AccountLockedException("Account is temporarily locked. Try again in " + minutesLeft + " minutes");
        }
        
        // Check for too many recent failed attempts
        if (user.getFailedLoginAttempts() >= MAX_FAILED_ATTEMPTS) {
            lockAccountTemporarily(user);
            throw new AccountLockedException("Account locked due to too many failed login attempts");
        }
    }
    
    /**
     * Check if password has expired
     */
    private void checkPasswordExpiry(UserSecurityEntity user) {
        if (user.getPasswordChangedAt() != null) {
            LocalDateTime passwordExpiry = user.getPasswordChangedAt().plusDays(PASSWORD_EXPIRY_DAYS);
            if (LocalDateTime.now().isAfter(passwordExpiry)) {
                throw new CredentialsExpiredException("Password has expired. Please change your password.");
            }
        }
        
        if (user.getForcePasswordChange()) {
            throw new CredentialsExpiredException("You must change your password before continuing.");
        }
    }
    
    /**
     * Check if credentials have expired
     */
    private void checkCredentialsExpiry(UserSecurityEntity user) {
        if (!user.getCredentialsNonExpired()) {
            throw new CredentialsExpiredException("Credentials have expired");
        }
    }
    
    /**
     * Handle failed login attempt
     */
    private void handleFailedLogin(UserSecurityEntity user, String ipAddress, String reason) {
        userRepository.incrementFailedLoginAttempts(user.getId());
        
        int attempts = user.getFailedLoginAttempts() + 1;
        
        log.warn("Failed login attempt #{} for user {} from IP {}: {}", 
                attempts, user.getUsername(), ipAddress, reason);
        
        if (attempts >= MAX_FAILED_ATTEMPTS) {
            lockAccountTemporarily(user);
            log.error("Account {} locked due to {} failed login attempts from IP {}", 
                     user.getUsername(), attempts, ipAddress);
        }
    }
    
    /**
     * Lock account temporarily
     */
    private void lockAccountTemporarily(UserSecurityEntity user) {
        LocalDateTime lockUntil = LocalDateTime.now().plusMinutes(LOCKOUT_DURATION_MINUTES);
        userRepository.lockAccount(user.getId(), lockUntil);
        
        auditService.logAccountLocked(user.getUsername(), lockUntil, "Too many failed login attempts");
    }
    
    /**
     * Record successful login
     */
    private void recordSuccessfulLogin(UserSecurityEntity user, String ipAddress, String userAgent) {
        userRepository.recordSuccessfulLogin(user.getId(), LocalDateTime.now(), ipAddress);
        
        // Update user agent hash for device tracking
        String userAgentHash = hashUserAgent(userAgent);
        user.setUserAgentHash(userAgentHash);
        
        log.info("Successful login for user {} from IP {} (attempts reset)", 
                user.getUsername(), ipAddress);
    }
    
    /**
     * Create authentication response
     */
    private AuthResponse createAuthResponse(UserSecurityEntity user, String ipAddress, String userAgent) {
        return AuthResponse.builder()
                .user(user)
                .loginTime(LocalDateTime.now())
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .sessionExpiryTime(LocalDateTime.now().plusHours(SESSION_TIMEOUT_HOURS))
                .requiresPasswordChange(user.requiresPasswordChange())
                .build();
    }
    
    /**
     * Hash user agent for device fingerprinting
     */
    private String hashUserAgent(String userAgent) {
        if (userAgent == null || userAgent.trim().isEmpty()) {
            return null;
        }
        // Simple hash for device tracking (not security-critical)
        return Integer.toHexString(userAgent.hashCode());
    }
    
    /**
     * Change user password with security validations
     */
    public Mono<Void> changePassword(Long userId, String currentPassword, String newPassword) {
        return Mono.fromRunnable(() -> {
            UserSecurityEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
            
            // Verify current password
            if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
                throw new InvalidCredentialsException("Current password is incorrect");
            }
            
            // Validate new password strength
            validatePasswordStrength(newPassword);
            
            // Update password
            String hashedPassword = passwordEncoder.encode(newPassword);
            userRepository.updatePassword(userId, hashedPassword, LocalDateTime.now());
            
            auditService.logPasswordChange(user.getUsername());
            
        }).then().subscribeOn(Schedulers.boundedElastic());
    }
    
    /**
     * Validate password strength
     */
    private void validatePasswordStrength(String password) {
        if (password == null || password.length() < 12) {
            throw new IllegalArgumentException("Password must be at least 12 characters long");
        }
        
        boolean hasUpper = password.chars().anyMatch(Character::isUpperCase);
        boolean hasLower = password.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        boolean hasSpecial = password.chars().anyMatch(ch -> "!@#$%^&*()_+-=[]{}|;:,.<>?".indexOf(ch) >= 0);
        
        if (!hasUpper || !hasLower || !hasDigit || !hasSpecial) {
            throw new IllegalArgumentException("Password must contain uppercase, lowercase, digit and special character");
        }
    }
    
    /**
     * Initialize default admin users on startup
     */
    @PostConstruct
    public void initializeDefaultUsers() {
        try {
            if (userRepository.countActiveUsers() == 0) {
                createDefaultAdminUser();
                log.info("Default admin user created");
            }
        } catch (Exception e) {
            log.error("Error initializing default users: {}", e.getMessage());
        }
    }
    
    /**
     * Create default admin user for initial system access
     */
    private void createDefaultAdminUser() {
        String defaultPassword = "ParkingAdmin2025!";
        String hashedPassword = passwordEncoder.encode(defaultPassword);
        
        UserSecurityEntity admin = UserSecurityEntity.builder()
                .username("admin")
                .password(hashedPassword)
                .email("admin@parking.local")
                .firstName("System")
                .lastName("Administrator")
                .role(UserSecurityEntity.Role.ADMIN)
                .enabled(true)
                .emailVerified(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .forcePasswordChange(true) // Force password change on first login
                .passwordChangedAt(LocalDateTime.now())
                .build();
        
        userRepository.save(admin);
        
        log.warn("Default admin user created with username 'admin' and temporary password '{}'. " +
                "CHANGE THIS PASSWORD IMMEDIATELY!", defaultPassword);
    }
}