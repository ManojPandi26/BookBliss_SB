package com.BookBliss.DTO.Admin.CategoryManagement;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CategorySearchCriteria {
    private String keyword;
    private Integer minBookCount;
    private Integer maxBookCount;
    private LocalDateTime createdAfter;
    private LocalDateTime createdBefore;
}
