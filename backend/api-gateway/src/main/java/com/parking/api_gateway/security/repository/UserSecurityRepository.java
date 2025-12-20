package com.parking.api_gateway.security.repository;

import com.parking.api_gateway.security.entity.UserSecurityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserSecurityRepository extends JpaRepository<UserSecurityEntity, Long> {
    
    // Basic user lookup
    Optional<UserSecurityEntity> findByUsername(String username);
    Optional<UserSecurityEntity> findByEmail(String email);
    
    // Security queries - only active users
    @Query("SELECT u FROM UserSecurityEntity u WHERE " +
           "(u.username = :login OR u.email = :login) AND " +
           "u.enabled = true AND " +
           "u.accountNonExpired = true AND " +
           "u.credentialsNonExpired = true AND " +
           "u.deletedAt IS NULL AND " +
           "(u.accountLockedUntil IS NULL OR u.accountLockedUntil < :now)")
    Optional<UserSecurityEntity> findActiveUserByLogin(@Param("login") String login, 
                                                       @Param("now") LocalDateTime now);
    
    // Password reset functionality
    Optional<UserSecurityEntity> findByPasswordResetToken(String token);
    
    @Modifying
    @Query("UPDATE UserSecurityEntity u SET " +
           "u.passwordResetToken = :token, " +
           "u.passwordResetExpiresAt = :expiresAt " +
           "WHERE u.id = :userId")
    void setPasswordResetToken(@Param("userId") Long userId, 
                               @Param("token") String token, 
                               @Param("expiresAt") LocalDateTime expiresAt);
    
    // Account management
    @Modifying
    @Query("UPDATE UserSecurityEntity u SET " +
           "u.enabled = :enabled " +
           "WHERE u.id = :userId")
    void setAccountEnabled(@Param("userId") Long userId, @Param("enabled") boolean enabled);
    
    @Modifying
    @Query("UPDATE UserSecurityEntity u SET " +
           "u.accountNonLocked = false, " +
           "u.accountLockedUntil = :lockedUntil " +
           "WHERE u.id = :userId")
    void lockAccount(@Param("userId") Long userId, @Param("lockedUntil") LocalDateTime lockedUntil);
    
    @Modifying
    @Query("UPDATE UserSecurityEntity u SET " +
           "u.accountNonLocked = true, " +
           "u.accountLockedUntil = null, " +
           "u.failedLoginAttempts = 0 " +
           "WHERE u.id = :userId")
    void unlockAccount(@Param("userId") Long userId);
    
    // Login tracking
    @Modifying
    @Query("UPDATE UserSecurityEntity u SET " +
           "u.lastLoginAt = u.currentLoginAt, " +
           "u.lastLoginIp = u.currentLoginIp, " +
           "u.currentLoginAt = :loginTime, " +
           "u.currentLoginIp = :ipAddress, " +
           "u.loginCount = u.loginCount + 1, " +
           "u.failedLoginAttempts = 0 " +
           "WHERE u.id = :userId")
    void recordSuccessfulLogin(@Param("userId") Long userId, 
                              @Param("loginTime") LocalDateTime loginTime, 
                              @Param("ipAddress") String ipAddress);
    
    @Modifying
    @Query("UPDATE UserSecurityEntity u SET " +
           "u.failedLoginAttempts = u.failedLoginAttempts + 1 " +
           "WHERE u.id = :userId")
    void incrementFailedLoginAttempts(@Param("userId") Long userId);
    
    // Password management
    @Modifying
    @Query("UPDATE UserSecurityEntity u SET " +
           "u.password = :hashedPassword, " +
           "u.passwordChangedAt = :now, " +
           "u.forcePasswordChange = false " +
           "WHERE u.id = :userId")
    void updatePassword(@Param("userId") Long userId, 
                       @Param("hashedPassword") String hashedPassword, 
                       @Param("now") LocalDateTime now);
    
    // Security checks
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByUsernameAndEnabledTrue(String username);
    
    // Admin queries
    @Query("SELECT COUNT(u) FROM UserSecurityEntity u WHERE " +
           "u.enabled = true AND u.deletedAt IS NULL")
    long countActiveUsers();
    
    @Query("SELECT COUNT(u) FROM UserSecurityEntity u WHERE " +
           "u.accountNonLocked = false AND u.deletedAt IS NULL")
    long countLockedUsers();
    
    @Query("SELECT u FROM UserSecurityEntity u WHERE " +
           "u.lastLoginAt BETWEEN :start AND :end")
    java.util.List<UserSecurityEntity> findUsersLoginBetween(@Param("start") LocalDateTime start, 
                                                             @Param("end") LocalDateTime end);
}