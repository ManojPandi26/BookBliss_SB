package com.BookBliss.DTO.ReadingSpace;


import com.BookBliss.Entity.ReadinSpace.UserReadingProgress;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;


@Data
@Builder
public class UserReadingProgressDTO {
    private Long id;

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Book ID is required")
    private Long bookId;

    @Min(value = 0, message = "Current page cannot be negative")
    private Integer currentPage;

    @Min(0) @Max(100)
    private Double readingPercentage;

    private UserReadingProgress.ReadingStatus status;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime lastReadAt;

}
