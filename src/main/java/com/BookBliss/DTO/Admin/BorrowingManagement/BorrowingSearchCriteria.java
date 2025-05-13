package com.BookBliss.DTO.Admin.BorrowingManagement;

import com.BookBliss.Entity.Borrowing.BorrowingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BorrowingSearchCriteria {
    // User search
    private String userKeyword; // For searching across username, email, etc.

    // Item search
    private String itemKeyword; // For searching across book title, author, journal title, etc.

    // Specific filters that can be used alongside keywords
    private Long bookId;
    private String isbn;
    private Long journalId;
    private String issn;
    private String itemType; // "BOOK", "JOURNAL", or null for both

    // Borrowing status
    private List<BorrowingStatus> statuses;

    // Date ranges
    private LocalDateTime borrowDateFrom;
    private LocalDateTime borrowDateTo;
    private LocalDateTime dueDateFrom;
    private LocalDateTime dueDateTo;
    private LocalDateTime returnDateFrom;
    private LocalDateTime returnDateTo;

    // Fine-related filters
    private Boolean hasOverdueFine;
    private Boolean isCurrentlyOverdue;
    private Boolean fineWaived;

    // Extension-related filters
    private Integer minExtensionCount;
    private Integer maxExtensionCount;

    // Pagination and sorting
    private String sortBy;
    private String sortDirection;
}