package com.BookBliss.Entity.ReadinSpace;

import com.BookBliss.Entity.Book;
import com.BookBliss.Entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_reading_bookmarks")
@Getter
@Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserReadingBookmark {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @NotNull(message = "Page number is required")
    @Min(value = 1, message = "Page number must be positive")
    private Integer pageNumber;

    @Size(max = 255, message = "Bookmark title cannot exceed 255 characters")
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}

