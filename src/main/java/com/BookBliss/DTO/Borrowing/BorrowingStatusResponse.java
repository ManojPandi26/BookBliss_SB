package com.BookBliss.DTO.Borrowing;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class BorrowingStatusResponse {
    private boolean borrowed;
    private LocalDateTime dueDate;
    private LocalDateTime borrowDate;
    private String message;
}
