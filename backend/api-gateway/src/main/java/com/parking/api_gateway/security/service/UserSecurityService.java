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
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10); // Match database hash strength

    // Security Configuration
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCKOUT_DURATION_MINUTES = 30;
    private static final int PASSWORD_EXPIRY_DAYS = 90;
    private static final int SESSION_TIMEOUT_HOURS = 8;
    
    /**
     * Authenticate user with comprehensive security checks (Reactive wrapper)
     */
    public Mono<AuthResponse> authenticateUser(AuthRequest request, String clientIpAddress, String userAgent) {
        return Mono.fromCallable(() -> authenticateUserSync(request, clientIpAddress, userAgent))
          .subscribeOn(Schedulers.boundedElastic())
          .doOnSuccess(response -> auditService.logSuccessfulLogin(response.getUser().getUsername(), clientIpAddress, userAgent))
          .doOnError(error -> auditService.logFailedLogin(request.getUsername(), clientIpAddress, userAgent, error.getMessage()));
    }

    /**
     * Synchronous authentication with transactional support
     */
    @Transactional
    private AuthResponse authenticateUserSync(AuthRequest request, String clientIpAddress, String userAgent) {
        log.info("🔐 [STEP 1] Starting authentication for user: {} from IP: {}", request.getUsername(), clientIpAddress);

        // 1. Find user
        log.debug("🔍 [STEP 1.1] Searching for active user in database...");
        UserSecurityEntity user = findActiveUser(request.getUsername())
            .orElseThrow(() -> {
                log.warn("❌ [STEP 1.1 FAILED] User not found: {}", request.getUsername());
                return new InvalidCredentialsException("Invalid username or password");
            });

        log.info("✓ [STEP 1.1 SUCCESS] User found: id={}, username={}, enabled={}, accountNonLocked={}",
                 user.getId(), user.getUsername(), user.getEnabled(), user.isAccountNonLocked());

        // 2. Pre-authentication security checks
        log.debug("🔒 [STEP 2] Validating account status...");
        try {
            validateAccountStatus(user);
            log.info("✓ [STEP 2 SUCCESS] Account status valid");
        } catch (Exception e) {
            log.error("❌ [STEP 2 FAILED] Account status validation failed: {}", e.getMessage());
            throw e;
        }

        // 3. Password verification
        log.debug("🔑 [STEP 3] Verifying password...");
        log.debug("🔍 [DEBUG] Password from request: length={}", request.getPassword() != null ? request.getPassword().length() : 0);
        log.debug("🔍 [DEBUG] Password hash from DB: {}", user.getPassword() != null ? user.getPassword().substring(0, Math.min(20, user.getPassword().length())) + "..." : "NULL");
        log.debug("🔍 [DEBUG] Encoder strength: BCrypt(10)");

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("❌ [STEP 3 FAILED] Password verification failed for user: {}", user.getUsername());
            log.debug("🔧 [STEP 3.1] Calling handleFailedLogin...");
            try {
                handleFailedLogin(user, clientIpAddress, "Invalid password");
                log.debug("✓ [STEP 3.1 SUCCESS] Failed login recorded");
            } catch (Exception e) {
                log.error("❌ [STEP 3.1 FAILED] Error recording failed login: {} - {}", e.getClass().getSimpleName(), e.getMessage());
                throw e;
            }
            throw new InvalidCredentialsException("Invalid username or password");
        }
        log.info("✓ [STEP 3 SUCCESS] Password verified");

        // 4. Post-authentication checks
        log.debug("📅 [STEP 4] Checking password and credentials expiry...");
        try {
            checkPasswordExpiry(user);
            checkCredentialsExpiry(user);
            log.info("✓ [STEP 4 SUCCESS] Expiry checks passed");
        } catch (Exception e) {
            log.error("❌ [STEP 4 FAILED] Expiry check failed: {}", e.getMessage());
            throw e;
        }

        // 5. Record successful login
        log.debug("📝 [STEP 5] Recording successful login...");
        try {
            recordSuccessfulLogin(user, clientIpAddress, userAgent);
            log.info("✓ [STEP 5 SUCCESS] Successful login recorded");
        } catch (Exception e) {
            log.error("❌ [STEP 5 FAILED] Error recording successful login: {} - {}", e.getClass().getSimpleName(), e.getMessage());
            throw e;
        }

        // 6. Create auth response
        log.debug("🎫 [STEP 6] Creating auth response...");
        AuthResponse response = createAuthResponse(user, clientIpAddress, userAgent);
        log.info("✅ [AUTHENTICATION SUCCESS] User {} authenticated successfully from IP {}", user.getUsername(), clientIpAddress);

        return response;
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
    @Transactional
    private void handleFailedLogin(UserSecurityEntity user, String ipAddress, String reason) {
        log.debug("🔧 [handleFailedLogin] Starting - userId: {}, currentAttempts: {}", user.getId(), user.getFailedLoginAttempts());

        try {
            log.debug("🔧 [handleFailedLogin] Incrementing failed attempts using entity method...");
            // Increment using entity and save
            user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
            user.setLastLoginIp(ipAddress);
            userRepository.save(user);
            log.debug("✓ [handleFailedLogin] Successfully incremented failed attempts");
        } catch (Exception e) {
            log.error("❌ [handleFailedLogin] ERROR incrementing attempts: {} - {}",
                     e.getClass().getSimpleName(), e.getMessage(), e);
            throw e;
        }

        int attempts = user.getFailedLoginAttempts();

        log.warn("Failed login attempt #{} for user {} from IP {}: {}", 
                attempts, user.getUsername(), ipAddress, reason);
        
        if (attempts >= MAX_FAILED_ATTEMPTS) {
            log.warn("🔒 [handleFailedLogin] MAX_FAILED_ATTEMPTS reached, locking account...");
            try {
                lockAccountTemporarily(user);
                log.error("Account {} locked due to {} failed login attempts from IP {}",
                         user.getUsername(), attempts, ipAddress);
            } catch (Exception e) {
                log.error("❌ [handleFailedLogin] ERROR locking account: {} - {}",
                         e.getClass().getSimpleName(), e.getMessage(), e);
                throw e;
            }
        }
    }
    
    /**
     * Lock account temporarily
     */
    private void lockAccountTemporarily(UserSecurityEntity user) {
        LocalDateTime lockUntil = LocalDateTime.now().plusMinutes(LOCKOUT_DURATION_MINUTES);
        log.debug("🔒 [lockAccountTemporarily] Locking user {} until {}", user.getUsername(), lockUntil);

        try {
            // Use entity method and save instead of native query
            user.setAccountLockedUntil(lockUntil);
            user.setAccountNonLocked(false);
            userRepository.save(user);
            log.debug("✓ [lockAccountTemporarily] Account locked successfully");
        } catch (Exception e) {
            log.error("❌ [lockAccountTemporarily] ERROR: {} - {}", e.getClass().getSimpleName(), e.getMessage(), e);
            throw e;
        }

        auditService.logAccountLocked(user.getUsername(), lockUntil, "Too many failed login attempts");
    }
    
    /**
     * Record successful login
     */
    private void recordSuccessfulLogin(UserSecurityEntity user, String ipAddress, String userAgent) {
        log.debug("📝 [recordSuccessfulLogin] Recording login for userId: {}", user.getId());

        try {
            log.debug("📝 [recordSuccessfulLogin] Updating user entity...");
            // Use entity methods and save instead of native query
            user.setLastLoginAt(user.getCurrentLoginAt());
            user.setLastLoginIp(user.getCurrentLoginIp());
            user.setCurrentLoginAt(LocalDateTime.now());
            user.setCurrentLoginIp(ipAddress);
            user.setLoginCount(user.getLoginCount() + 1);
            user.setFailedLoginAttempts(0); // Reset failed attempts
            user.setAccountLockedUntil(null); // Clear any lock

            // Update user agent hash for device tracking
            String userAgentHash = hashUserAgent(userAgent);
            user.setUserAgentHash(userAgentHash);

            userRepository.save(user);
            log.debug("✓ [recordSuccessfulLogin] Login recorded in database");
        } catch (Exception e) {
            log.error("❌ [recordSuccessfulLogin] ERROR: {} - {}", e.getClass().getSimpleName(), e.getMessage(), e);
            throw e;
        }


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