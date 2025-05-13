package com.BookBliss.DTO.Reviews;

import lombok.Data;
import jakarta.validation.constraints.*;

@Data
public class ReviewsDTO {
    private Long id;

    @NotNull(message = "User ID is required")
    private Long userId;

    private Long bookId;
    private Long journalId;

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be between 1 and 5")
    @Max(value = 5, message = "Rating must be between 1 and 5")
    private Integer rating;

    private String comment;
}
