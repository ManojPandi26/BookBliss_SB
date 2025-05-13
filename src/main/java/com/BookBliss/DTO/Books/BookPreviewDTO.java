package com.BookBliss.DTO.Books;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class BookPreviewDTO {
    private Long id;
    private String title;
    private String author;
    private String coverImageUrl;
    private Integer availableCopies;
    private List<String> categories;
}
