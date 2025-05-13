package com.BookBliss.DTO.Admin.BorrowingManagement;

import com.BookBliss.Entity.Borrowing.BorrowingStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BorrowingStatusUpdateDTO {
    @NotNull(message = "Status cannot be null")
    private BorrowingStatus status;

    private String adminNotes;
    private Boolean notifyUser;
}
