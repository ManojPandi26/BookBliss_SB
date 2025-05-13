package com.BookBliss.DTO.Books;

import lombok.Data;
import java.util.List;

@Data
public class BookDetailsDTO {
    private Long id;
    private String title;
    private String author;
    private String publisher;
    private Integer publicationYear;
    private String coverImageUrl;
    private String isbn;
    private String edition;
    private String description;
    private int availableCopies;
    private int totalCopies;
    private List<String> categories;


    private Double averageRatingGreaterThan;
    private Long ratingCount;
    private Long WishlistedCount;
    private Long borrowCount;
    private Boolean isNew;  // Books added within last week



}
