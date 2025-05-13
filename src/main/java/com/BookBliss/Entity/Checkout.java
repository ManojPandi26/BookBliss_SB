package com.BookBliss.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "checkouts", indexes = {
        @Index(name = "idx_checkout_user", columnList = "user_id"),
        @Index(name = "idx_checkout_bookshelf", columnList = "bookshelf_id"),
        @Index(name = "idx_checkout_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Checkout {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToOne
    @JoinColumn(name = "bookshelf_id", nullable = false)
    private MyBookshelf bookshelf;

    @Column(nullable = false)
    private String checkoutCode;

    @NotNull(message = "Borrowing days cannot be null")
    @Min(value = 1, message = "Borrowing days must be at least 1")
    @Column(nullable = false)
    private Integer borrowingDays;

    @NotNull(message = "Due date cannot be null")
    @Column(nullable = false)
    private LocalDate dueDate;

    @NotNull(message = "Status cannot be null")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CheckoutStatus status;

    @Size(max = 500, message = "Additional notes cannot exceed 500 characters")
    @Column(columnDefinition = "TEXT")
    private String additionalNotes;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "returned_at")
    private LocalDateTime returnedAt;

    // Enum for checkout status
    public enum CheckoutStatus {
        PENDING,         // Checkout initiated but not confirmed
        CONFIRMED,       // Checkout confirmed, waiting for pickup
        BORROWED,        // Books have been borrowed
        RETURNED,        // All books have been returned
        OVERDUE,         // Books are past due date
        CANCELLED        // Checkout was cancelled
    }
}
