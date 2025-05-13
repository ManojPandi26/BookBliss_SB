package com.BookBliss.DTO.ReadingSpace;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserReadingBookmarkDTO {
    private Long id;

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Book ID is required")
    private Long bookId;

    @NotNull(message = "Page number is required")
    @Min(value = 1, message = "Page number must be positive")
    private Integer pageNumber;

    @Size(max = 255, message = "Bookmark title cannot exceed 255 characters")
    private String title;

    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
