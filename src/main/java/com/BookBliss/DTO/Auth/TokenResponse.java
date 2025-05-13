package com.BookBliss.DTO.Auth;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TokenResponse {
    private String accessToken;
    private String refreshToken;
    private Long accessTokenExpiresAt;
    private Long refreshTokenExpiresAt;
}