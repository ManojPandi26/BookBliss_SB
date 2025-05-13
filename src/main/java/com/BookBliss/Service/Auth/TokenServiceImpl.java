package com.BookBliss.Service.Auth;

import com.BookBliss.DTO.Auth.TokenResponse;
import com.BookBliss.Entity.Token;
import com.BookBliss.Entity.User;
import com.BookBliss.Exception.AuthenticationException;
import com.BookBliss.Exception.PasswordResetException;
import com.BookBliss.Repository.TokenRepository;
import com.BookBliss.Repository.UserRepository;
import com.BookBliss.Utils.JwtTokenProvider;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {
    private static final Logger log = LoggerFactory.getLogger(TokenServiceImpl.class);

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;
    private final TokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final HttpServletRequest request;

    @Value("${jwt.refresh-token-renewal-threshold-hours:24}")
    private long refreshTokenRenewalThresholdHours;

    @Value("${jwt.token-cleanup-batch-size:100}")
    private int tokenCleanupBatchSize;

    @Value("${jwt.max-tokens-per-user:5}")
    private int maxTokensPerUser;

    // In-memory cache for quick token validation without DB hits
    private final Cache<String, String> tokenCache = Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.HOURS)
            .maximumSize(10_000)
            .recordStats()
            .build();

    // Cache for blacklisted tokens
    private final Cache<String, Boolean> tokenBlacklist = Caffeine.newBuilder()
            .expireAfterWrite(24, TimeUnit.HOURS)
            .maximumSize(10_000)
            .recordStats()
            .build();

    // Cache for token metadata (used for rate limiting)
    private final Cache<String, Token> tokenMetadataCache = Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.HOURS)
            .maximumSize(10_000)
            .build();

    /**
     * Creates a new token of the specified type for a user
     *
     * @param user The user to create the token for
     * @param tokenType The type of token to create
     * @param tokenValue The token value (JWT)
     * @param expiresAt When the token expires
     * @return The created Token entity
     */
    @Transactional
    @Override
    public Token createToken(User user, Token.TokenType tokenType, String tokenValue, LocalDateTime expiresAt) {
        // Enforce token limits per user if necessary
        if (tokenType == Token.TokenType.REFRESH) {
            enforceTokenLimits(user, tokenType);
        }

        Token token = Token.builder()
                .tokenValue(tokenValue)
                .user(user)
                .tokenType(tokenType)
                .expiresAt(expiresAt)
                .revoked(false)
                .used(false)
                .ipAddress(getClientIP())
                .userAgent(request.getHeader("User-Agent"))
                .deviceInfo(extractDeviceInfo(request.getHeader("User-Agent")))
                .tokenIdentifier(UUID.randomUUID().toString())
                .build();

        Token savedToken = tokenRepository.save(token);

        // For refresh tokens, cache them
        if (tokenType == Token.TokenType.REFRESH) {
            tokenCache.put(tokenValue, user.getUsername());
            tokenMetadataCache.put(tokenValue, savedToken);
        }

        log.info("Token created for user: {}, type: {}, expires: {}", user.getUsername(), tokenType, expiresAt);
        return savedToken;
    }

    /**
     * Creates an access token for a user with standard expiration
     *
     * @param user The user to create the token for
     * @param authentication The authentication object
     * @return The token response containing the access token
     */
    @Transactional
    @Override
    public TokenResponse createAccessToken(User user, Authentication authentication) {
        String accessToken = jwtTokenProvider.generateToken(authentication);
        LocalDateTime expiryTime = LocalDateTime.now().plusSeconds(jwtTokenProvider.getTokenExpiration() / 1000);

        createToken(user, Token.TokenType.ACCESS, accessToken, expiryTime);

        return TokenResponse.builder()
                .accessToken(accessToken)
                .accessTokenExpiresAt(System.currentTimeMillis() + jwtTokenProvider.getTokenExpiration())
                .build();
    }

    /**
     * Creates both access and refresh tokens for a user
     *
     * @param user The user to create tokens for
     * @param authentication The authentication object
     * @return The token response containing both tokens
     */
    @Transactional
    @Override
    public TokenResponse createTokenPair(User user, Authentication authentication) {
        // Generate tokens
        String accessToken = jwtTokenProvider.generateToken(authentication);
        String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);

        // Calculate expiry times
        LocalDateTime accessExpiryTime = LocalDateTime.now().plusSeconds(jwtTokenProvider.getTokenExpiration() / 1000);
        LocalDateTime refreshExpiryTime = LocalDateTime.now().plusSeconds(jwtTokenProvider.getRefreshTokenExpiration() / 1000);

        // Create token entities
        createToken(user, Token.TokenType.ACCESS, accessToken, accessExpiryTime);
        createToken(user, Token.TokenType.REFRESH, refreshToken, refreshExpiryTime);

        // Return token response
        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .accessTokenExpiresAt(System.currentTimeMillis() + jwtTokenProvider.getTokenExpiration())
                .refreshTokenExpiresAt(System.currentTimeMillis() + jwtTokenProvider.getRefreshTokenExpiration())
                .build();
    }

    /**
     * Finds all valid tokens for a user of a specific type
     *
     * @param user The user to find tokens for
     * @param tokenType The type of token to find
     * @return List of valid tokens
     */
    @Override
    public List<Token> findValidTokensByUser(User user, Token.TokenType tokenType) {
        return tokenRepository.findValidTokensByUser(user, tokenType, LocalDateTime.now());
    }

    @Override
    @Transactional
    public void storeRefreshToken(String refreshToken, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AuthenticationException("User not found: " + username));

        // Store in cache for quick lookups
        tokenCache.put(refreshToken, username);

        // Create token and store in database
        createToken(user, Token.TokenType.REFRESH, refreshToken,
                LocalDateTime.now().plusSeconds(jwtTokenProvider.getRefreshTokenExpiration() / 1000));

        log.info("Refresh token stored for user: {}", username);
    }

    @Override
    @Transactional
    public void invalidateRefreshToken(String refreshToken) {
        if (refreshToken == null) {
            return;
        }

        // Remove from cache
        tokenCache.invalidate(refreshToken);
        tokenMetadataCache.invalidate(refreshToken);

        // Update in database
        int updated = tokenRepository.revokeToken(refreshToken, LocalDateTime.now());

        // Add to blacklist
        tokenBlacklist.put(refreshToken, true);

        log.info("Refresh token invalidated: {} records updated", updated);
    }

    /**
     * Validate a token by token identifier
     *
     * @param tokenIdentifier The unique identifier of the token
     * @return true if valid, false otherwise
     */
    @Override
    public boolean validateTokenByIdentifier(String tokenIdentifier) {
        Optional<Token> token = tokenRepository.findByTokenIdentifier(tokenIdentifier);

        if (token.isEmpty()) {
            return false;
        }

        Token tokenEntity = token.get();
        return !tokenEntity.isExpired() && !tokenEntity.isRevoked() && !tokenEntity.isUsed();
    }

    @Override
    public void blacklistToken(String token) {
        if (token != null) {
            tokenBlacklist.put(token, true);
            log.debug("Token added to blacklist");
        }
    }

    @Override
    public boolean isTokenBlacklisted(String token) {
        return token != null && tokenBlacklist.getIfPresent(token) != null;
    }

    @Override
    @Transactional
    public TokenResponse refreshTokens(String refreshToken) {
        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new AuthenticationException("Refresh token is required");
        }

        // Check if token is blacklisted
        if (isTokenBlacklisted(refreshToken)) {
            throw new AuthenticationException("Refresh token has been invalidated");
        }

        try {
            // Validate the refresh token
            if (!jwtTokenProvider.validateRefreshToken(refreshToken)) {
                throw new AuthenticationException("Invalid refresh token");
            }

            // Check if token exists in database
            Optional<Token> tokenEntity = tokenRepository.findByTokenValueAndTokenType(
                    refreshToken, Token.TokenType.REFRESH);

            if (tokenEntity.isEmpty() || tokenEntity.get().isRevoked() || tokenEntity.get().isExpired()) {
                throw new AuthenticationException("Refresh token is invalid or expired");
            }

            Token token = tokenEntity.get();
            User user = token.getUser();

            // Update last used timestamp
            token.setLastUsedAt(LocalDateTime.now());
            tokenRepository.save(token);

            // Load user details and create authentication
            UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
            );

            // Set authentication in security context
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Always generate new access token
            String newAccessToken = jwtTokenProvider.generateToken(authentication);

            // Check if refresh token needs renewal
            String newRefreshToken = refreshToken;
            boolean isRefreshTokenRenewed = false;
            LocalDateTime refreshTokenExpiry = token.getExpiresAt();
            LocalDateTime renewalThreshold = LocalDateTime.now().plusHours(refreshTokenRenewalThresholdHours);

            // Generate new refresh token only if current one is close to expiration
            if (refreshTokenExpiry.isBefore(renewalThreshold)) {
                newRefreshToken = jwtTokenProvider.generateRefreshToken(authentication);

                // Mark old token as used
                token.markAsUsed();
                tokenRepository.save(token);

                // Create new token record
                createToken(user, Token.TokenType.REFRESH, newRefreshToken,
                        LocalDateTime.now().plusSeconds(jwtTokenProvider.getRefreshTokenExpiration() / 1000));

                isRefreshTokenRenewed = true;

                log.info("Refresh token renewed for user: {}", user.getUsername());
            }

            // Create token for the access token
            createToken(user, Token.TokenType.ACCESS, newAccessToken,
                    LocalDateTime.now().plusSeconds(jwtTokenProvider.getTokenExpiration() / 1000));

            log.info("Access token refreshed for user: {}", user.getUsername());

            Date refreshTokenExpiryDate = isRefreshTokenRenewed ?
                    new Date(System.currentTimeMillis() + jwtTokenProvider.getRefreshTokenExpiration()) :
                    java.sql.Timestamp.valueOf(refreshTokenExpiry);

            return TokenResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(newRefreshToken)
                    .accessTokenExpiresAt(System.currentTimeMillis() + jwtTokenProvider.getTokenExpiration())
                    .refreshTokenExpiresAt(refreshTokenExpiryDate.getTime())
                    .build();

        } catch (AuthenticationException e) {
            log.error("Refresh token error: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during token refresh: {}", e.getMessage());
            throw new AuthenticationException("Error processing refresh token: " + e.getMessage());
        }
    }

    @Override
    public boolean validateAccessToken(String token, UserDetails userDetails) {
        if (isTokenBlacklisted(token)) {
            return false;
        }

        // Check if token exists in database
        if (tokenRepository.existsByTokenValueAndRevokedFalse(token)) {
            return jwtTokenProvider.validateToken(token, userDetails);
        }

        return false;
    }

    @Override
    public void logCacheStats() {
        log.info("Token Cache - Size: {}, Hit Rate: {:.2f}%",
                tokenCache.estimatedSize(),
                tokenCache.stats().hitRate() * 100);

        log.info("Token Blacklist Cache - Size: {}",
                tokenBlacklist.estimatedSize());

        log.info("Token Metadata Cache - Size: {}",
                tokenMetadataCache.estimatedSize());
    }

    @Override
    @Transactional
    public void revokeAllUserTokens(User user, Token.TokenType tokenType) {
        LocalDateTime now = LocalDateTime.now();
        int revokedCount = tokenRepository.revokeAllUserTokens(user, tokenType, now);

        // Clear any cached tokens for this user
        if (tokenType == Token.TokenType.REFRESH) {
            // Evict from cache based on username
            tokenCache.asMap().entrySet().removeIf(entry -> entry.getValue().equals(user.getUsername()));
        }

        log.info("Revoked {} tokens for user: {}", revokedCount, user.getUsername());
    }

    /**
     * Create an email verification token for a user
     *
     * @param user The user to create the token for
     * @return The token value
     */
    @Transactional
    @Override
    public String createEmailVerificationToken(User user) {
        String tokenValue = UUID.randomUUID().toString();

        // Email verification tokens typically last 24 hours
        LocalDateTime expiryTime = LocalDateTime.now().plusHours(24);

        createToken(user, Token.TokenType.EMAIL_VERIFICATION, tokenValue, expiryTime);

        return tokenValue;
    }

    /**
     * Create a password reset token for a user
     *
     * @param user The user to create the token for
     * @return The token value
     */
    @Transactional
    @Override
    public String createPasswordResetToken(User user) {
        String tokenValue = UUID.randomUUID().toString();

        // Password reset tokens typically last 1 hour
        LocalDateTime expiryTime = LocalDateTime.now().plusHours(1);

        createToken(user, Token.TokenType.PASSWORD_RESET, tokenValue, expiryTime);

        return tokenValue;
    }

    @Scheduled(cron = "0 0 * * * *") // Run every hour
    @Transactional
    public void cleanupExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        int deletedCount = tokenRepository.deleteAllExpiredTokens(now);
        log.info("Deleted {} expired tokens", deletedCount);
    }

    @Transactional(readOnly = true)
    @Override
    public Token validatePasswordResetToken(String tokenValue) {
        log.debug("Validating password reset token");

        if (tokenValue == null || tokenValue.isEmpty()) {
            throw new PasswordResetException("Token cannot be empty");
        }

        // Find token in repository
        Token token = tokenRepository.findByTokenValueAndTokenType(tokenValue, Token.TokenType.PASSWORD_RESET)
                .orElseThrow(() -> new PasswordResetException("Invalid password reset token"));

        // Check if token is expired
        if (token.isExpired()) {
            throw new PasswordResetException("Password reset token has expired");
        }

        // Check if token has been used
        if (token.isUsed()) {
            throw new PasswordResetException("Password reset token has already been used");
        }

        // Check if token has been revoked
        if (token.isRevoked()) {
            throw new PasswordResetException("Password reset token has been revoked");
        }

        log.info("Password reset token validated for user: {}", token.getUser().getUsername());
        return token;
    }


    @Transactional(readOnly = true)
    @Override
    public Token validateEmailVerificationToken(String tokenValue) {
        log.debug("Validating email verification token");

        if (tokenValue == null || tokenValue.isEmpty()) {
            throw new AuthenticationException("Token cannot be empty");
        }

        // Find token in repository
        Token token = tokenRepository.findByTokenValueAndTokenType(tokenValue, Token.TokenType.EMAIL_VERIFICATION)
                .orElseThrow(() -> new AuthenticationException("Invalid email verification token"));

        // Check if token is expired
        if (token.isExpired()) {
            throw new AuthenticationException("Email verification token has expired");
        }

        // Check if token has been used
        if (token.isUsed()) {
            throw new AuthenticationException("Email verification token has already been used");
        }

        // Check if token has been revoked
        if (token.isRevoked()) {
            throw new AuthenticationException("Email verification token has been revoked");
        }

        log.info("Email verification token validated for user: {}", token.getUser().getUsername());
        return token;
    }

    @Override
    public Optional<Token> findByTokenValueAndType(String tokenValue, Token.TokenType tokenType) {
        return tokenRepository.findByTokenValueAndTokenType(tokenValue, tokenType);
    }

    /**
     * Marks a token as used
     *
     * @param token The token to mark as used
     */
    @Transactional
    @Override
    public void markTokenAsUsed(Token token) {
        if (token == null) {
            return;
        }

        token.setUsed(true);
        token.setLastUsedAt(LocalDateTime.now());
        tokenRepository.save(token);

        log.debug("Token marked as used: {}", token.getTokenValue());
    }

    /**
     * Enforce maximum number of tokens per user
     * If user exceeds limit, oldest tokens will be revoked
     */
    @Transactional
    private void enforceTokenLimits(User user, Token.TokenType tokenType) {
        List<Token> userTokens = tokenRepository.findAllByUserAndTokenTypeAndRevokedFalse(user, tokenType);

        if (userTokens.size() >= maxTokensPerUser) {
            log.info("User {} has reached token limit. Revoking oldest tokens.", user.getUsername());

            // Sort tokens by creation date (oldest first)
            userTokens.sort((t1, t2) -> t1.getCreatedAt().compareTo(t2.getCreatedAt()));

            // Revoke oldest tokens to make room for new one
            int tokensToRevoke = userTokens.size() - maxTokensPerUser + 1;
            for (int i = 0; i < tokensToRevoke; i++) {
                Token oldToken = userTokens.get(i);
                oldToken.markAsRevoked();
                tokenRepository.save(oldToken);

                // Also remove from cache
                tokenCache.invalidate(oldToken.getTokenValue());
                tokenBlacklist.put(oldToken.getTokenValue(), true);

                log.debug("Revoked old token for user: {}", user.getUsername());
            }
        }
    }

    private String getClientIP() {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty() || "unknown".equalsIgnoreCase(xfHeader)) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }

    private String extractDeviceInfo(String userAgent) {
        if (userAgent == null) {
            return "Unknown";
        }

        // Simple device detection
        if (userAgent.contains("Mobile") || userAgent.contains("Android") || userAgent.contains("iPhone")) {
            return "Mobile";
        } else if (userAgent.contains("Tablet") || userAgent.contains("iPad")) {
            return "Tablet";
        } else {
            return "Desktop";
        }
    }
}