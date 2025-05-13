package com.BookBliss.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.Cache;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tokens",
        indexes = {
                @Index(name = "idx_token_value", columnList = "token_value"),
                @Index(name = "idx_token_user_id", columnList = "user_id"),
                @Index(name = "idx_token_type", columnList = "token_type")
        })
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Token {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token_value", nullable = false, length = 500)
    private String tokenValue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "token_type", nullable = false)
    private TokenType tokenType;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "is_revoked", nullable = false)
    private boolean revoked;

    @Column(name = "is_used", nullable = false)
    private boolean used;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 255)
    private String userAgent;

    @Column(name = "device_info", length = 255)
    private String deviceInfo;

    @Column(name = "token_identifier", nullable = false, unique = true)
    private String tokenIdentifier;

    @PrePersist
    public void prePersist() {
        if (tokenIdentifier == null) {
            tokenIdentifier = UUID.randomUUID().toString();
        }
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public void markAsRevoked() {
        this.revoked = true;
        this.revokedAt = LocalDateTime.now();
    }

    public void markAsUsed() {
        this.used = true;
        this.lastUsedAt = LocalDateTime.now();
    }

    public boolean isValid() {
        return !isExpired() && !revoked && !used;
    }

    public enum TokenType {
        ACCESS,
        REFRESH,
        PASSWORD_RESET,
        EMAIL_VERIFICATION
    }
}