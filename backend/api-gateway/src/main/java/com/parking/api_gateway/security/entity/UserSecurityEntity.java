package com.parking.api_gateway.security.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.net.InetAddress;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSecurityEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String username;
    
    @Column(name = "password_hash", nullable = false)
    private String password;
    
    @Column(unique = true)
    private String email;
    
    @Column(name = "first_name")
    private String firstName;
    
    @Column(name = "last_name")
    private String lastName;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "user_role", nullable = false)
    private Role role;
    
    // Account Status
    @Builder.Default
    @Column(nullable = false)
    private Boolean enabled = true;
    
    @Builder.Default
    @Column(name = "email_verified", nullable = false)
    private Boolean emailVerified = false;
    
    @Builder.Default
    @Column(name = "account_non_expired", nullable = false)
    private Boolean accountNonExpired = true;
    
    @Builder.Default
    @Column(name = "account_non_locked", nullable = false)  
    private Boolean accountNonLocked = true;
    
    @Builder.Default
    @Column(name = "credentials_non_expired", nullable = false)
    private Boolean credentialsNonExpired = true;
    
    // Password Security
    @Column(name = "password_reset_token")
    private String passwordResetToken;
    
    @Column(name = "password_reset_expires_at")
    private LocalDateTime passwordResetExpiresAt;
    
    @Column(name = "password_changed_at")
    private LocalDateTime passwordChangedAt;
    
    @Builder.Default
    @Column(name = "force_password_change", nullable = false)
    private Boolean forcePasswordChange = false;
    
    // Brute Force Protection
    @Builder.Default
    @Column(name = "failed_login_attempts", nullable = false)
    private Integer failedLoginAttempts = 0;
    
    @Column(name = "account_locked_until")
    private LocalDateTime accountLockedUntil;
    
    // Login Tracking
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;
    
    @Column(name = "last_login_ip")
    private String lastLoginIp;
    
    @Column(name = "current_login_at")
    private LocalDateTime currentLoginAt;
    
    @Column(name = "current_login_ip")
    private String currentLoginIp;
    
    @Builder.Default
    @Column(name = "login_count", nullable = false)
    private Integer loginCount = 0;
    
    // Two-Factor Authentication
    @Builder.Default
    @Column(name = "two_factor_enabled", nullable = false)
    private Boolean twoFactorEnabled = false;
    
    @Column(name = "two_factor_secret")
    private String twoFactorSecret;
    
    @ElementCollection
    @CollectionTable(
        name = "user_backup_codes",
        joinColumns = @JoinColumn(name = "user_id")
    )
    @Column(name = "backup_code")
    private List<String> backupCodes;
    
    // Session Management
    @Builder.Default
    @Column(name = "active_sessions_limit", nullable = false)
    private Integer activeSessionsLimit = 3;
    
    @Column(name = "last_password_check_at")
    private LocalDateTime lastPasswordCheckAt;
    
    // Security Questions
    @Column(name = "security_question_hash")
    private String securityQuestionHash;
    
    @Column(name = "security_answer_hash")
    private String securityAnswerHash;
    
    // Terms & Privacy
    @Column(name = "terms_accepted_at")
    private LocalDateTime termsAcceptedAt;
    
    @Column(name = "privacy_accepted_at")
    private LocalDateTime privacyAcceptedAt;
    
    // Additional Metadata
    @Column(name = "user_agent_hash")
    private String userAgentHash;
    
    @Builder.Default
    @Column(name = "preferred_language")
    private String preferredLanguage = "en";
    
    @Builder.Default
    @Column(name = "timezone")
    private String timezone = "UTC";
    
    @Column(name = "avatar_url")
    private String avatarUrl;
    
    // Audit Fields
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Soft Delete
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deleted_by")
    private UserSecurityEntity deletedBy;
    
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        passwordChangedAt = now;
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Security Helper Methods
    public boolean isAccountNonLocked() {
        if (accountLockedUntil == null) {
            return accountNonLocked;
        }
        return LocalDateTime.now().isAfter(accountLockedUntil);
    }
    
    public boolean requiresPasswordChange() {
        return forcePasswordChange || 
               (passwordChangedAt != null && 
                passwordChangedAt.plusDays(90).isBefore(LocalDateTime.now()));
    }
    
    public boolean canAttemptLogin() {
        return enabled && 
               accountNonExpired && 
               isAccountNonLocked() && 
               credentialsNonExpired &&
               deletedAt == null;
    }
    
    public void recordFailedLogin(String ipAddress) {
        this.failedLoginAttempts++;
        this.lastLoginIp = ipAddress;
        
        // Lock account after 5 failed attempts for 30 minutes
        if (this.failedLoginAttempts >= 5) {
            this.accountLockedUntil = LocalDateTime.now().plusMinutes(30);
            this.accountNonLocked = false;
        }
    }
    
    public void recordSuccessfulLogin(String ipAddress) {
        this.lastLoginAt = this.currentLoginAt;
        this.lastLoginIp = this.currentLoginIp;
        this.currentLoginAt = LocalDateTime.now();
        this.currentLoginIp = ipAddress;
        this.loginCount++;
        this.failedLoginAttempts = 0;
        this.accountLockedUntil = null;
        this.accountNonLocked = true;
        this.lastPasswordCheckAt = LocalDateTime.now();
    }
    
    public enum Role {
        USER, MANAGER, ADMIN, OPERATOR, SECURITY_ADMIN
    }
}