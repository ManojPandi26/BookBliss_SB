package com.BookBliss.Entity.ReadinSpace;

import com.BookBliss.Entity.Book;
import com.BookBliss.Entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "user_reading_progress")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserReadingProgress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Min(value = 0, message = "Current page cannot be negative")
    private Integer currentPage;

    @Min(0) @Max(100)
    private Double readingPercentage;

    @Enumerated(EnumType.STRING)
    private ReadingStatus status;

    @CreationTimestamp
    private LocalDateTime startedAt;

    private LocalDateTime completedAt;

    @UpdateTimestamp
    private LocalDateTime lastReadAt;

    @OneToMany(mappedBy = "userReadingProgress", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ReadingSession> readingSessions = new HashSet<>();

    public enum ReadingStatus {
        NOT_STARTED,
        IN_PROGRESS,
        ON_HOLD,
        COMPLETED,
        ABANDONED
    }
}

