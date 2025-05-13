package com.BookBliss.Entity.ReadinSpace;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.*;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "reading_sessions")
@Getter
@Setter @NoArgsConstructor @AllArgsConstructor
@Builder
public class ReadingSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_book_progress_id", nullable = false)
    private UserReadingProgress userReadingProgress;

    @Column(nullable = false)
    private LocalDateTime startTime;

    private LocalDateTime endTime;

    @Min(value = 0, message = "Pages read cannot be negative")
    private Integer pagesRead;

    @Min(value = 0, message = "Duration cannot be negative")
    private Integer durationMinutes;

    @Column(columnDefinition = "TEXT")
    private String sessionNotes;
}
