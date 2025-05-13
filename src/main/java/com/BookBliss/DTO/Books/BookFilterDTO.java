package com.BookBliss.DTO.Books;

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
public class BookFilterDTO {
    private Boolean available;
    private Integer yearFrom;
    private Integer yearTo;
    private List<String> categories;
    private Double averageRatingGreaterThan;
    private String keyword;
    private String title;
    private String author;
    private String isbn;
    private String publisher;
    private Integer availableCopiesMin;
    private Integer availableCopiesMax;
    private LocalDateTime createdFrom;
    private LocalDateTime createdTo;
}
