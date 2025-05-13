package com.BookBliss.DTO.Admin.CategoryManagement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookSummaryDTO {
    private Long id;
    private String title;
    private String author;
    private String isbn;
}
