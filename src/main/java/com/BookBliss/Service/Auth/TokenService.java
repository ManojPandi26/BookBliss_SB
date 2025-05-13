package com.BookBliss.Service.Auth;

import com.BookBliss.DTO.Auth.TokenResponse;
import com.BookBliss.Entity.Token;
import com.BookBliss.Entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TokenService {
    @Transactional
    Token createToken(User user, Token.TokenType tokenType, String tokenValue, LocalDateTime expiresAt);

    @Transactional
    TokenResponse createAccessToken(User user, Authentication authentication);

    @Transactional
    TokenResponse createTokenPair(User user, Authentication authentication);

    List<Token> findValidTokensByUser(User user, Token.TokenType tokenType);

    /**
     * Store a refresh token in the database and cache
     */
    void storeRefreshToken(String refreshToken, String username);

    /**
     * Invalidate a refresh token
     */
    void invalidateRefreshToken(String refreshToken);

    boolean validateTokenByIdentifier(String tokenIdentifier);

    /**
     * Blacklist an access token
     */
    void blacklistToken(String token);

    /**
     * Check if a token is blacklisted
     */
    boolean isTokenBlacklisted(String token);

    /**
     * Generate a new access token using a valid refresh token.
     * Only generates a new refresh token if the current one is near expiration.
     */
    TokenResponse refreshTokens(String refreshToken);

    /**
     * Validate an access token
     */
    boolean validateAccessToken(String token, UserDetails userDetails);

    /**
     * Log cache statistics
     */
    void logCacheStats();

    /**
     * Revoke all tokens for a specific user
     */
    void revokeAllUserTokens(User user, Token.TokenType tokenType);

    @Transactional
    String createEmailVerificationToken(User user);

    @Transactional
    String createPasswordResetToken(User user);

    @Transactional(readOnly = true)
    Token validatePasswordResetToken(String tokenValue);

    @Transactional
    void markTokenAsUsed(Token token);


    Token validateEmailVerificationToken(String tokenValue);

    Optional<Token> findByTokenValueAndType(String tokenValue, Token.TokenType tokenType);

}