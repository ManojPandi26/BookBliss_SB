package com.BookBliss.Entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "journals", indexes = {
        @Index(name = "idx_journal_title", columnList = "title"),
        @Index(name = "idx_journal_publisher", columnList = "publisher")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Journal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title cannot exceed 255 characters")
    @Column(nullable = false, length = 255)
    private String title;

    @NotBlank(message = "Publisher is required")
    @Size(max = 255, message = "Publisher name cannot exceed 255 characters")
    @Column(nullable = false, length = 255)
    private String publisher;

    @NotBlank(message = "ISSN is required")
    @Pattern(regexp = "^(?:ISSN(?:-8)?:? )?(?=[0-9X]{8}$|(?=(?:[0-9]+[- ]){3})([0-9]+[- ]){2}[0-9X]+$)[0-9]{4}[- ]?[0-9]{3}[0-9X]$",
            message = "Invalid ISSN format")
    @Column(nullable = false, unique = true, length = 8)
    private String issn;

    @NotBlank(message = "Issue number is required")
    @Size(max = 50, message = "Issue number cannot exceed 50 characters")
    @Column(name = "issue_number", nullable = false, length = 50)
    private String issueNumber;

    @Size(max = 50, message = "Volume number cannot exceed 50 characters")
    @Column(name = "volume_number", length = 50)
    private String volumeNumber;

    @NotNull(message = "Publication date is required")
    @Column(name = "publication_date", nullable = false)
    private LocalDate publicationDate;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "cover_image_url", length = 255)
    private String coverImageUrl;

    @NotNull(message = "Available copies cannot be null")
    @Min(value = 0, message = "Available copies cannot be negative")
    @Column(name = "available_copies")
    private Integer availableCopies;

    @NotNull(message = "Total copies cannot be null")
    @Min(value = 1, message = "Total copies must be at least 1")
    @Column(name = "total_copies")
    private Integer totalCopies;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}

