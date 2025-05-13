package com.BookBliss.DTO.Auth;

import com.BookBliss.Entity.User;

import lombok.Data;

@Data
public class UserDto {
    // User registration  Response Dto.....
    private Long id;
    private String username;
    private String email;
    private User.UserRole role;

}
