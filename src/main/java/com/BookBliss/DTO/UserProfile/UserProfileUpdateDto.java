package com.BookBliss.DTO.UserProfile;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UserProfileUpdateDto {
    @Size(max = 100)
    private String fullName;
    
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid phone number")
    private String phoneNumber;
    
    private String address;

}
