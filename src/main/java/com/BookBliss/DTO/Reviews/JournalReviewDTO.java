package com.BookBliss.DTO.Reviews;

import lombok.Data;

@Data
public class JournalReviewDTO {
    private Long id;
    private String title;
    private String publisher;
    private String coverImageUrl;
}
