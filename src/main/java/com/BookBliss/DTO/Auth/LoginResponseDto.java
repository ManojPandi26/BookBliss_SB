package com.BookBliss.DTO.Auth;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponseDto {
	private Long id;
    private String username;
    private String email;
    private String role;
    private TokenResponse tokens;
    private String profileImageUrl;
}
