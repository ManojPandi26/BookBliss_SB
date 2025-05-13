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
public class AdminBookDetailsDTO {
    private Long id;
    private String title;
    private String author;
    private String publisher;
    private Integer publicationYear;
    private String isbn;
    private String edition;
    private String description;
    private String coverImageUrl;
    private Integer availableCopies;
    private Integer totalCopies;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<String> categories;
//    private String status;
//    private Boolean isDeleted;
//    private LocalDateTime deletedAt;
//    private String createdBy;
//    private String lastModifiedBy;
    private Long borrowCount;
    private Double averageRating;
    private Long reviewCount;


}
