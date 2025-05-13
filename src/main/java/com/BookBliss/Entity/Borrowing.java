package com.BookBliss.Entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;


@Entity
@Table(name = "borrowings" , indexes = {
        @Index(name = "idx_borrowing_user", columnList = "user_id"),
        @Index(name = "idx_borrowing_book", columnList = "book_id"),
        @Index(name = "idx_borrowing_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Borrowing {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "journal_id")
    private Journal journal;

    @CreationTimestamp
    @Column(name = "borrow_date", nullable = false, updatable = false)
    private LocalDateTime borrowDate;

    @NotNull(message = "Due date is required")
    @Column(name = "due_date", nullable = false)
    private LocalDateTime dueDate;

    @Column(name = "return_date")
    private LocalDateTime returnDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BorrowingStatus status;

    @Column(name = "fine_amount", precision = 10, scale = 2)
    private BigDecimal fineAmount;

    public enum BorrowingStatus {
        BORROWED,
        RETURNED,
        OVERDUE
    }
}
