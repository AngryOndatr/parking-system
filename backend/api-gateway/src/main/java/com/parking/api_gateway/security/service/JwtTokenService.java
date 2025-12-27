package com.parking.api_gateway.security.service;

import com.parking.api_gateway.security.entity.UserSecurityEntity;
import com.parking.api_gateway.security.exception.InvalidCredentialsException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import jakarta.annotation.PostConstruct;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtTokenService {
    
    private final SecurityAuditService auditService;
    private final RedisTemplate<String, Object> redisTemplate;
    
    // JWT Configuration
    @Value("${security.jwt.secret:ParkingSystemSecretKey2025!VeryLongAndSecureKey123456789}")
    private String jwtSecret;
    
    @Value("${security.jwt.access-token-expiration:3600}")  // 1 hour
    private long accessTokenExpiration;
    
    @Value("${security.jwt.refresh-token-expiration:86400}") // 24 hours
    private long refreshTokenExpiration;
    
    @Value("${security.jwt.issuer:parking-system}")
    private String issuer;
    
    private SecretKey signingKey;
    
    // In-memory blacklist fallback (Redis preferred)
    private final Map<String, LocalDateTime> tokenBlacklist = new ConcurrentHashMap<>();
    
    // Redis keys
    private static final String BLACKLIST_KEY_PREFIX = "jwt:blacklist:";
    private static final String REFRESH_TOKEN_KEY_PREFIX = "jwt:refresh:";
    private static final String USER_SESSIONS_KEY_PREFIX = "jwt:sessions:";
    
    @PostConstruct
    public void initializeJwtService() {
        // Create strong signing key from secret
        this.signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        
        // Ensure secret is strong enough
        if (jwtSecret.length() < 64) {
            log.warn("JWT secret is shorter than recommended 64 characters. Consider using a longer secret.");
        }
        
        log.info("JWT Token Service initialized with issuer: {}", issuer);
    }
    
    /**
     * Create access token with user details and claims
     */
    public String createAccessToken(UserSecurityEntity user, String ipAddress, String userAgent) {
        String jti = UUID.randomUUID().toString();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpiration * 1000);
        
        String token = Jwts.builder()
                .setId(jti)
                .setIssuer(issuer)
                .setSubject(user.getUsername())
                .setAudience("parking-system-api")
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .claim("userId", user.getId())
                .claim("role", user.getRole().name())
                .claim("email", user.getEmail())
                .claim("firstName", user.getFirstName())
                .claim("lastName", user.getLastName())
                .claim("ipAddress", ipAddress)
                .claim("userAgentHash", hashUserAgent(userAgent))
                .claim("tokenType", "ACCESS")
                .signWith(signingKey, SignatureAlgorithm.HS512)
                .compact();
        
        // Store user session info in Redis
        storeUserSession(user.getId(), jti, ipAddress, userAgent, expiryDate);
        
        auditService.logTokenCreated(user.getUsername(), "ACCESS", ipAddress);
        
        log.debug("Access token created for user {} with JTI {}", user.getUsername(), jti);
        return token;
    }
    
    /**
     * Create refresh token for token renewal
     */
    public String createRefreshToken(UserSecurityEntity user, String ipAddress) {
        String jti = UUID.randomUUID().toString();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenExpiration * 1000);
        
        String token = Jwts.builder()
                .setId(jti)
                .setIssuer(issuer)
                .setSubject(user.getUsername())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .claim("userId", user.getId())
                .claim("ipAddress", ipAddress)
                .claim("tokenType", "REFRESH")
                .signWith(signingKey, SignatureAlgorithm.HS512)
                .compact();
        
        // Store refresh token in Redis with expiration
        try {
            String refreshKey = REFRESH_TOKEN_KEY_PREFIX + jti;
            redisTemplate.opsForValue().set(refreshKey, user.getId().toString(), refreshTokenExpiration, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("Failed to store refresh token in Redis: {}", e.getMessage());
        }

        auditService.logTokenCreated(user.getUsername(), "REFRESH", ipAddress);
        
        log.debug("Refresh token created for user {} with JTI {}", user.getUsername(), jti);
        return token;
    }
    
    /**
     * Validate access token and extract claims
     */
    public Mono<Claims> validateAccessToken(String token, String clientIpAddress) {
        return Mono.fromCallable(() -> {
            try {
                // Parse and validate token
                Claims claims = Jwts.parser()  // Updated from parserBuilder()
                        .setSigningKey(signingKey)
                        .requireIssuer(issuer)
                        .build()
                        .parseClaimsJws(token)
                        .getBody();
                
                // Check if token is blacklisted
                String jti = claims.getId();
                if (isTokenBlacklisted(jti)) {
                    throw new InvalidCredentialsException("Token has been revoked");
                }
                
                // Validate token type
                String tokenType = claims.get("tokenType", String.class);
                if (!"ACCESS".equals(tokenType)) {
                    throw new InvalidCredentialsException("Invalid token type");
                }
                
                // Additional security checks
                validateTokenSecurity(claims, clientIpAddress);
                
                auditService.logTokenValidated(claims.getSubject(), "ACCESS", true);
                
                return claims;
                
            } catch (JwtException | IllegalArgumentException e) {
                log.warn("Invalid JWT token: {}", e.getMessage());
                auditService.logTokenValidated("unknown", "ACCESS", false);
                throw new InvalidCredentialsException("Invalid or expired token");
            }
        });
    }
    
    /**
     * Validate refresh token and return user ID
     */
    public Mono<Long> validateRefreshToken(String token) {
        return Mono.fromCallable(() -> {
            try {
                Claims claims = Jwts.parser()  // Updated from parserBuilder()
                        .setSigningKey(signingKey)
                        .requireIssuer(issuer)
                        .build()
                        .parseClaimsJws(token)
                        .getBody();
                
                String jti = claims.getId();
                String tokenType = claims.get("tokenType", String.class);
                
                if (!"REFRESH".equals(tokenType)) {
                    throw new InvalidCredentialsException("Invalid token type");
                }
                
                // Check if refresh token exists in Redis
                String refreshKey = REFRESH_TOKEN_KEY_PREFIX + jti;
                Object userIdObj = redisTemplate.opsForValue().get(refreshKey);

                if (userIdObj == null) {
                    throw new InvalidCredentialsException("Refresh token not found or expired");
                }
                
                // Safely convert to Long
                Long userId;
                if (userIdObj instanceof String) {
                    userId = Long.parseLong((String) userIdObj);
                } else if (userIdObj instanceof Number) {
                    userId = ((Number) userIdObj).longValue();
                } else {
                    throw new InvalidCredentialsException("Invalid refresh token data");
                }

                auditService.logTokenValidated(claims.getSubject(), "REFRESH", true);
                
                return userId;
                
            } catch (JwtException | IllegalArgumentException e) {
                log.warn("Invalid refresh token: {}", e.getMessage());
                auditService.logTokenValidated("unknown", "REFRESH", false);
                throw new InvalidCredentialsException("Invalid or expired refresh token");
            }
        });
    }
    
    /**
     * Blacklist token (logout, security breach, etc.)
     */
    public Mono<Void> blacklistToken(String token, String reason) {
        return Mono.fromRunnable(() -> {
            try {
                Claims claims = Jwts.parser()  // Updated from parserBuilder()
                        .setSigningKey(signingKey)
                        .requireIssuer(issuer)
                        .build()
                        .parseClaimsJws(token)
                        .getBody();
                
                String jti = claims.getId();
                Date expiration = claims.getExpiration();
                
                // Add to Redis blacklist
                try {
                    String blacklistKey = BLACKLIST_KEY_PREFIX + jti;
                    long ttl = (expiration.getTime() - System.currentTimeMillis()) / 1000;
                    if (ttl > 0) {
                        redisTemplate.opsForValue().set(blacklistKey, reason, ttl, TimeUnit.SECONDS);
                    }
                } catch (Exception e) {
                    log.warn("Failed to blacklist token in Redis: {}", e.getMessage());
                }
                
                // Add to in-memory fallback
                tokenBlacklist.put(jti, LocalDateTime.ofInstant(expiration.toInstant(), ZoneId.systemDefault()));
                
                auditService.logTokenBlacklisted(claims.getSubject(), jti, reason);
                
                log.debug("Token blacklisted - JTI: {}, Reason: {}", jti, reason);
                
            } catch (JwtException e) {
                log.warn("Cannot blacklist invalid token: {}", e.getMessage());
            }
        });
    }
    
    /**
     * Invalidate all user sessions (force logout from all devices)
     */
    public Mono<Void> invalidateAllUserSessions(Long userId, String reason) {
        return Mono.fromRunnable(() -> {
            try {
                String sessionsKey = USER_SESSIONS_KEY_PREFIX + userId;

                @SuppressWarnings("unchecked")
                Set<Object> sessionJtis = redisTemplate.opsForSet().members(sessionsKey);

                if (sessionJtis != null && !sessionJtis.isEmpty()) {
                    for (Object jtiObj : sessionJtis) {
                        String jti = jtiObj.toString();
                        String blacklistKey = BLACKLIST_KEY_PREFIX + jti;
                        redisTemplate.opsForValue().set(blacklistKey, reason, accessTokenExpiration, TimeUnit.SECONDS);
                    }

                    // Clear user sessions
                    redisTemplate.delete(sessionsKey);

                    log.info("Invalidated {} sessions for user {} - Reason: {}",
                            sessionJtis.size(), userId, reason);
                }
            } catch (Exception e) {
                log.warn("Failed to invalidate user sessions in Redis for user {}: {}", userId, e.getMessage());
            }
        });
    }
    
    /**
     * Check if token is blacklisted
     */
    private boolean isTokenBlacklisted(String jti) {
        // Check Redis first
        try {
            String blacklistKey = BLACKLIST_KEY_PREFIX + jti;
            Boolean hasKey = redisTemplate.hasKey(blacklistKey);
            if (Boolean.TRUE.equals(hasKey)) {
                return true;
            }
        } catch (Exception e) {
            log.warn("Failed to check token blacklist in Redis: {}", e.getMessage());
        }
        
        // Check in-memory fallback
        LocalDateTime expiry = tokenBlacklist.get(jti);
        if (expiry != null) {
            if (LocalDateTime.now().isBefore(expiry)) {
                return true;
            } else {
                // Remove expired entry
                tokenBlacklist.remove(jti);
            }
        }
        
        return false;
    }
    
    /**
     * Validate token security properties
     */
    private void validateTokenSecurity(Claims claims, String clientIpAddress) {
        // IP address validation (optional, can be disabled for mobile apps)
        String tokenIpAddress = claims.get("ipAddress", String.class);
        if (tokenIpAddress != null && !tokenIpAddress.equals(clientIpAddress)) {
            log.warn("Token IP mismatch - Token IP: {}, Client IP: {}, User: {}", 
                    tokenIpAddress, clientIpAddress, claims.getSubject());
            // Note: Not throwing exception as IPs can change (mobile networks, proxies)
            // Consider implementing more sophisticated IP validation logic
        }
    }
    
    /**
     * Store user session information
     */
    private void storeUserSession(Long userId, String jti, String ipAddress, String userAgent, Date expiry) {
        try {
            String sessionsKey = USER_SESSIONS_KEY_PREFIX + userId;

            // Add session JTI to user's session set
            redisTemplate.opsForSet().add(sessionsKey, jti);

            // Set expiration for the session set
            long ttl = (expiry.getTime() - System.currentTimeMillis()) / 1000;
            if (ttl > 0) {
                redisTemplate.expire(sessionsKey, ttl, TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            log.warn("Failed to store user session in Redis for user {}: {}", userId, e.getMessage());
        }
    }
    
    /**
     * Hash user agent for device fingerprinting
     */
    private String hashUserAgent(String userAgent) {
        if (userAgent == null || userAgent.trim().isEmpty()) {
            return "unknown";
        }
        
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(userAgent.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash).substring(0, 16); // Use first 16 chars
        } catch (Exception e) {
            log.warn("Error hashing user agent: {}", e.getMessage());
            return "hash-error";
        }
    }
    
    /**
     * Convert bytes to hex string
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
    
    /**
     * Cleanup expired tokens from in-memory blacklist
     */
    public void cleanupExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        tokenBlacklist.entrySet().removeIf(entry -> now.isAfter(entry.getValue()));
        
        log.debug("Token cleanup completed. Remaining blacklisted tokens: {}", 
                tokenBlacklist.size());
    }
}