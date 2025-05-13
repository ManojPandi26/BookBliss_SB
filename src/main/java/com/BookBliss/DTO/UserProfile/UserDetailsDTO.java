package com.BookBliss.DTO.UserProfile;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDetailsDTO {
	
	private Long id;

	private String username;
	
	private String email;
	
	private String fullName;
	
	private String phoneNumber;
	
	private String address;
	
	private String role;

	private Boolean emailVerified;

	private Boolean isActive;

	private LocalDateTime updatedAt;
	
	private LocalDateTime createdAt;
	
	private LocalDateTime LastLogin;
	
	private String profileImageUrl;
}
