package com.BookBliss.DTO.Reviews;

import com.BookBliss.DTO.CategoryDTO;
import lombok.Data;
import java.util.Set;

@Data
public class BookReviewDTO {
    private Long id;
    private String title;
    private String author;
    private String coverImageUrl;
    private Set<CategoryDTO> categories;
}
