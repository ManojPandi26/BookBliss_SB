package com.BookBliss.DTO.Borrowing;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.BookBliss.Entity.Borrowing.BorrowingStatus;

@Data
public class BorrowingDTO {
    private Long id;
    
    @NotNull(message = "User ID is required")
    private Long userId;
    
    private Long bookId;
    private Long journalId;
    private LocalDateTime borrowDate;
    
    @NotNull(message = "Due date is required")
    private LocalDateTime dueDate;
    
    private LocalDateTime returnDate;
    private BorrowingStatus status;
    private BigDecimal fineAmount;
    private String userName;
    private String bookTitle;
    private String journalTitle;
}
