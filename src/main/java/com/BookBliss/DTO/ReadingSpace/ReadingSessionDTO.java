package com.BookBliss.DTO.ReadingSpace;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReadingSessionDTO {
    private Long id;

    @NotNull(message = "User Reading Progress ID is required")
    private Long userReadingProgressId;

    @NotNull(message = "Start time is required")
    private LocalDateTime startTime;

    private LocalDateTime endTime;

    @Min(value = 0, message = "Pages read cannot be negative")
    private Integer pagesRead;

    @Min(value = 0, message = "Duration cannot be negative")
    private Integer durationMinutes;

    // session notes can be displayed
    private String sessionNotes;
}
