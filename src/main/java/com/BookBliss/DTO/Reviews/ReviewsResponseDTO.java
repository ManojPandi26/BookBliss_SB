package com.BookBliss.DTO.Reviews;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReviewsResponseDTO {
    private Long id;
    private UserReviewDTO user;
    private BookReviewDTO book;
    private JournalReviewDTO journal;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
}
