package com.BookBliss.Entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "my_bookshelves", indexes = {
        @Index(name = "idx_bookshelf_user", columnList = "user_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class MyBookshelf {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookshelfStatus status;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "bookshelf", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<BookshelfItem> bookshelfItems = new HashSet<>();

    // Enum for bookshelf status
    public enum BookshelfStatus {
        ACTIVE,          // Current cart/bookshelf
        CHECKOUT,        // In checkout process
        BORROWED,        // Books have been borrowed
        COMPLETED,       // All books returned
        CANCELLED        // Checkout was cancelled
    }

    // Helper methods to manage bookshelf items

    public int getTotalItems() {
        return bookshelfItems.stream()
                .mapToInt(BookshelfItem::getQuantity)
                .sum();
    }
}