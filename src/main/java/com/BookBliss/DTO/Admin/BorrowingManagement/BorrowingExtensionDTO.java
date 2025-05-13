package com.BookBliss.DTO.Admin.BorrowingManagement;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BorrowingExtensionDTO {
    // Either provide days to extend or a specific new due date
    @Min(value = 1, message = "Extension days must be at least 1")
    private Integer extensionDays;

    private LocalDateTime newDueDate;

    @NotNull(message = "Notification preference cannot be null")
    private Boolean notifyUser;

    private String adminNotes;
    private String extensionReason;
}