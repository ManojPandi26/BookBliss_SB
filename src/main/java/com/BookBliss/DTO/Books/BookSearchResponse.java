package com.BookBliss.DTO.Books;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookSearchResponse {
    private Long id;
    private String title;
    private String author;
    private String isbn;
}
