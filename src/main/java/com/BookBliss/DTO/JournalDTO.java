package com.BookBliss.DTO;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class JournalDTO {
    private Long id;

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title cannot exceed 255 characters")
    private String title;

    @NotBlank(message = "Publisher is required")
    @Size(max = 255, message = "Publisher name cannot exceed 255 characters")
    private String publisher;

    @NotBlank(message = "Issue number is required")
    @Size(max = 50, message = "Issue number cannot exceed 50 characters")
    private String issueNumber;

    @Size(max = 50, message = "Volume number cannot exceed 50 characters")
    private String volumeNumber;

    @NotNull(message = "Publication date is required")
    private LocalDate publicationDate;

    private String description;
    private String coverImageUrl;

    @NotNull(message = "Available copies cannot be null")
    @Min(value = 0, message = "Available copies cannot be negative")
    private Integer availableCopies;

    @NotNull(message = "Total copies cannot be null")
    @Min(value = 1, message = "Total copies must be at least 1")
    private Integer totalCopies;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
