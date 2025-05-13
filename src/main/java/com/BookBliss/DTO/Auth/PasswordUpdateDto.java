package com.BookBliss.DTO.Auth;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class PasswordUpdateDto {
    @NotBlank(message = "Current password is required")
    private String currentPassword;
    
    @NotBlank(message = "New password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String newPassword;
    
}