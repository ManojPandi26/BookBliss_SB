package com.BookBliss.DTO.ReadingSpace;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserReadingNoteDTO {
    private Long id;

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Book ID is required")
    private Long bookId;

    @NotNull(message = "Page number is required")
    @Min(value = 1, message = "Page number must be positive")
    private Integer pageNumber;

    @NotBlank(message = "Note content is required")
    private String content;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
