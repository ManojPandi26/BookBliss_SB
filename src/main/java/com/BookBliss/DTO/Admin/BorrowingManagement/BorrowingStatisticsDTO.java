package com.BookBliss.DTO.Admin.BorrowingManagement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BorrowingStatisticsDTO {
    private Long totalBorrowings;
    private Long totalActiveBorrowings;
    private Long totalOverdueBorrowings;
    private Long totalReturnedBorrowings;
    private BigDecimal totalFinesCollected;
    private BigDecimal totalFinesOutstanding;

    private List<Map<String, Object>> borrowingsByStatus;
    private List<Map<String, Object>> borrowingsTrendByMonth;
    private List<Map<String, Object>> mostBorrowedBooks;
    private List<Map<String, Object>> mostBorrowedJournals;
    private List<Map<String, Object>> usersBorrowingMost;
    private List<Map<String, Object>> usersWithMostOverdue;

    private Double averageBorrowingDuration;
    private Double averageOverdueDays;


}
