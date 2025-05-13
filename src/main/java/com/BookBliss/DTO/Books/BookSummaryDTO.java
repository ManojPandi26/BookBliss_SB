package com.BookBliss.DTO.Books;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookSummaryDTO {
    private Long id;
    private String title;
    private String author;
    private String publisher;
    private Integer publicationYear;
    private String isbn;
    private Double averageRating;
    private Long ratingCount;
    private Long borrowCount;
    private int availableCopies;
    private List<String> categories;
    private String coverImageUrl;
    private Boolean isNew;

}
