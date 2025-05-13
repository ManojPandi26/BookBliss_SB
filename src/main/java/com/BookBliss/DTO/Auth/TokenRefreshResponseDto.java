package com.BookBliss.DTO.Auth;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TokenRefreshResponseDto {
    private String accessToken;
    private String refreshToken;
    private Long expiresAt;
}
