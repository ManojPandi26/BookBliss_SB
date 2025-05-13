package com.BookBliss.DTO.Admin.BorrowingManagement;

import com.BookBliss.Entity.Borrowing.BorrowingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminBorrowingDetailsDTO {
    private Long id;

    // User details
    private Long userId;
    private String username;
    private String userEmail;

    // Item details (either book or journal)
    private Long itemId;
    private String itemType; // "BOOK" or "JOURNAL"
    private String itemTitle;
    private String itemAuthor;
    private String isbn; // For books
    private String issn; // For journals

    // Borrowing details
    private LocalDateTime borrowDate;
    private LocalDateTime dueDate;
    private LocalDateTime returnDate;
    private BorrowingStatus status;
    private BigDecimal fineAmount;
//    private Integer extensionCount;

    // Audit information
//    private String createdBy;
//    private LocalDateTime createdAt;
//    private String lastModifiedBy;
//    private LocalDateTime lastModifiedAt;

    // Additional admin-specific fields
//    private Boolean fineWaived;
//    private String adminNotes;
    private Long daysOverdue;
    private Boolean isRenewable;
}
