package com.BookBliss.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs", indexes = {
        @Index(name = "idx_audit_entity_id", columnList = "entityId"),
        @Index(name = "idx_audit_entity_type", columnList = "entityType"),
        @Index(name = "idx_audit_action", columnList = "action"),
        @Index(name = "idx_audit_actor_id", columnList = "actorId"),
        @Index(name = "idx_audit_timestamp", columnList = "timestamp")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EntityType entityType;

    @Column(nullable = false)
    private Long entityId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActionType action;

    @Column(columnDefinition = "TEXT")
    private String details;

    @Column(nullable = false)
    private Long actorId;

    @Column(length = 50)
    private String actorName;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;

    @Column(length = 39)  // IPv6 max length
    private String ipAddress;

    // Enums for common entity types and actions
    public enum EntityType {
        USER,
        BOOK,
        CATEGORY,
        AUTHOR,
        LOAN,
        RESERVATION
    }

    public enum ActionType {
        CREATE,
        READ,
        UPDATE,
        DELETE,
        LOGIN,
        LOGOUT,
        NEW_USER,
        PASSWORD_CHANGE,
        EMAIL_VERIFY,
        STATUS_CHANGE,
        ROLE_CHANGE
    }
}
