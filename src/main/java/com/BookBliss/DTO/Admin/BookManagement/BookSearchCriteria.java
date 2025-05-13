package com.BookBliss.DTO.Admin.BookManagement;


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
public class BookSearchCriteria {
    private String keyword;
    private String publisher;
    private Integer publicationYearFrom;
    private Integer publicationYearTo;
    private List<String> categories;
    private Boolean available;
    private Integer availableCopiesLessThan;
    private Integer availableCopiesGreaterThan;
    private Double averageRatingGreaterThan;
    private LocalDateTime createdAfter;
    private LocalDateTime createdBefore;
    private LocalDateTime updatedAfter;
    private LocalDateTime updatedBefore;
    private String sortBy;
    private String sortDirection;
//    private Boolean includeDeleted;
    private Long borrowCountGreaterThan;
//    private String status;
}
