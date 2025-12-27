package com.parking.api_gateway.security.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class SecurityAuditService {
    
    // In-memory audit cache for high-frequency events
    private final Map<String, Integer> loginAttemptCache = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> lastAuditFlush = new ConcurrentHashMap<>();
    
    /**
     * Log successful login
     */
    public void logSuccessfulLogin(String username, String ipAddress, String userAgent) {
        log.info("AUDIT: Successful login - User: {}, IP: {}, UserAgent: {}, Time: {}", 
                username, ipAddress, truncateUserAgent(userAgent), LocalDateTime.now());
        
        // Reset failed attempt counter for this IP
        loginAttemptCache.remove(ipAddress);
    }
    
    /**
     * Log failed login attempt
     */
    public void logFailedLogin(String username, String ipAddress, String userAgent, String reason) {
        // Increment attempt counter for this IP
        loginAttemptCache.merge(ipAddress, 1, Integer::sum);
        
        log.warn("AUDIT: Failed login - User: {}, IP: {}, Reason: {}, UserAgent: {}, Time: {}, Total Attempts: {}", 
                username, ipAddress, reason, truncateUserAgent(userAgent), 
                LocalDateTime.now(), loginAttemptCache.get(ipAddress));
    }
    
    /**
     * Log account locked event
     */
    public void logAccountLocked(String username, LocalDateTime lockUntil, String reason) {
        log.error("AUDIT: Account locked - User: {}, Locked until: {}, Reason: {}, Time: {}", 
                username, lockUntil, reason, LocalDateTime.now());
    }
    
    /**
     * Log password change
     */
    public void logPasswordChange(String username) {
        log.info("AUDIT: Password changed - User: {}, Time: {}", 
                username, LocalDateTime.now());
    }
    
    /**
     * Log JWT token creation
     */
    public void logTokenCreated(String username, String tokenType, String ipAddress) {
        log.info("AUDIT: Token created - User: {}, Type: {}, IP: {}, Time: {}", 
                username, tokenType, ipAddress, LocalDateTime.now());
    }
    
    /**
     * Log JWT token validation
     */
    public void logTokenValidated(String username, String tokenType, boolean isValid) {
        if (isValid) {
            log.debug("AUDIT: Token validated - User: {}, Type: {}, Time: {}", 
                    username, tokenType, LocalDateTime.now());
        } else {
            log.warn("AUDIT: Token validation failed - User: {}, Type: {}, Time: {}", 
                    username, tokenType, LocalDateTime.now());
        }
    }
    
    /**
     * Log JWT token blacklisted
     */
    public void logTokenBlacklisted(String username, String tokenJti, String reason) {
        log.warn("AUDIT: Token blacklisted - User: {}, JTI: {}, Reason: {}, Time: {}", 
                username, tokenJti, reason, LocalDateTime.now());
    }
    
    /**
     * Log user logout
     */
    public void logUserLogout(String username, String ipAddress, String reason) {
        log.info("AUDIT: User logout - User: {}, IP: {}, Reason: {}, Time: {}", 
                username, ipAddress, reason, LocalDateTime.now());
    }
    
    /**
     * Log security configuration change
     */
    public void logSecurityConfigChange(String adminUsername, String changeType, String details) {
        log.warn("AUDIT: Security config changed - Admin: {}, Change: {}, Details: {}, Time: {}", 
                adminUsername, changeType, details, LocalDateTime.now());
    }
    
    /**
     * Log suspicious activity
     */
    public void logSuspiciousActivity(String username, String ipAddress, String activity, String details) {
        log.error("AUDIT: Suspicious activity - User: {}, IP: {}, Activity: {}, Details: {}, Time: {}", 
                username, ipAddress, activity, details, LocalDateTime.now());
    }
    
    /**
     * Get login attempt count for IP address
     */
    public int getLoginAttemptCount(String ipAddress) {
        return loginAttemptCache.getOrDefault(ipAddress, 0);
    }
    
    /**
     * Check if IP should be temporarily blocked due to too many attempts
     */
    public boolean shouldBlockIp(String ipAddress, int maxAttempts, int timeWindowMinutes) {
        int attempts = getLoginAttemptCount(ipAddress);
        
        if (attempts >= maxAttempts) {
            LocalDateTime lastFlush = lastAuditFlush.get(ipAddress);
            if (lastFlush == null || 
                LocalDateTime.now().minusMinutes(timeWindowMinutes).isAfter(lastFlush)) {
                // Reset counter after time window
                loginAttemptCache.remove(ipAddress);
                lastAuditFlush.put(ipAddress, LocalDateTime.now());
                return false;
            }
            return true;
        }
        
        return false;
    }
    
    /**
     * Truncate user agent for logging (security - don't log full user agent)
     */
    private String truncateUserAgent(String userAgent) {
        if (userAgent == null) return "unknown";
        if (userAgent.length() > 100) {
            return userAgent.substring(0, 100) + "...";
        }
        return userAgent;
    }
    
    /**
     * Cleanup old audit data periodically
     */
    @Transactional
    public void cleanupOldAuditData() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
        
        // Remove old entries from cache
        lastAuditFlush.entrySet().removeIf(entry -> entry.getValue().isBefore(cutoff));
        
        log.debug("Audit cache cleanup completed. Remaining entries: {}", 
                lastAuditFlush.size());
    }
}