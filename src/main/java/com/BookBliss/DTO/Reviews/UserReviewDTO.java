package com.BookBliss.DTO.Reviews;

import lombok.Data;

@Data
public class UserReviewDTO {
    private Long id;
    private String username;
    private String fullName;
    private String profileImageUrl;
}
