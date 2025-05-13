package com.BookBliss.Service.Auth;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.BookBliss.DTO.Auth.*;
import com.BookBliss.DTO.OTP.OtpVerificationDto;
import com.BookBliss.Entity.AuditLog;
import com.BookBliss.Entity.OtpVerification;
import com.BookBliss.Entity.Token;
import com.BookBliss.Events.Authentications.UserLoginEvent;
import com.BookBliss.Events.Authentications.UserLogoutEvent;
import com.BookBliss.Exception.*;
import com.BookBliss.Mapper.UserMapper;
import com.BookBliss.Repository.OtpRepository;
import com.BookBliss.Service.Audit.AuditService;
import com.BookBliss.Service.Email.EmailServiceImpl;
import com.BookBliss.Utils.JwtAuthenticationFilter;
import com.BookBliss.Utils.JwtTokenProvider;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.BookBliss.Entity.User;
import com.BookBliss.Repository.UserRepository;

/**
 * Implementation of authentication service handling user authentication, registration,
 * and related security operations.
 *
 * @author ManojPandi
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailServiceImpl emailService;

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserMapper userMapper;

    private final ApplicationEventPublisher eventPublisher;
    private final LoginAttemptService loginAttemptService;
    private final HttpServletRequest request;

    private final TokenService tokenService;

    private final AuditService auditService;

    private final OtpRepository otpRepository;

    // User cache for quick user lookups
    private final Cache<String, User> userCache = Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.HOURS)
            .maximumSize(10_000)
            .recordStats()
            .build();

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);


    /**
     * Authenticates a user and generates authentication tokens.
     *
     * @param loginRequest The login request containing username and password
     * @return LoginResponseDto Containing user details and authentication tokens
     * @throws AuthenticationException if credentials are invalid or account is locked
     * @throws UserNotFoundException if user doesn't exist
     *
     * @see LoginResponseDto
     */
    @Override
    @Transactional
    public LoginResponseDto login(LoginRequestDto loginRequest) {
        String ip = loginAttemptService.getClientIP();
        String userAgent = request.getHeader("User-Agent");

        // Check if IP is blocked
        if (loginAttemptService.isBlocked(ip)) {
            LocalDateTime blockedUntil = loginAttemptService.getBlockedUntil(ip);
            throw new AuthenticationException("Account is locked. Try again after: " + blockedUntil);
        }

        try {
            // Attempt authentication
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Get user from cache or database
            User user = getUserFromCacheOrDatabase(loginRequest.getUsername());

            // Reset login attempts on successful login
            loginAttemptService.loginSucceeded(ip);

            // Update last login time
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);
            userCache.put(user.getUsername(), user);
            logCacheStats();

            // Log the user in Audit table
            auditService.logActivity(
                    AuditLog.EntityType.USER,
                    user.getId(),
                    AuditLog.ActionType.LOGIN,
                    user.getUsername() + "has loggedin on " + LocalDateTime.now()
            );

            // Generate tokens using TokenService
            TokenResponse tokens = tokenService.createTokenPair(user, authentication);

            // Publish login event
            eventPublisher.publishEvent(new UserLoginEvent(
                    this,
                    user.getId(),
                    ip,
                    userAgent,
                    true
            ));

            return LoginResponseDto.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .role(user.getRole().name())
                    .tokens(tokens)
                    .profileImageUrl(user.getProfileImageUrl())
                    .build();

        } catch (AuthenticationException e) {
            // Record failed attempt
            loginAttemptService.loginFailed(ip, null);

            // Get remaining attempts
            int remainingAttempts = loginAttemptService.getRemainingAttempts(ip);

            // Publish failed login event
            eventPublisher.publishEvent(new UserLoginEvent(
                    this,
                    null,
                    ip,
                    userAgent,
                    false
            ));

            throw new AuthenticationException("Invalid credentials. " +
                    (remainingAttempts > 0 ? remainingAttempts + " attempts remaining." :
                            "Account will be locked for security reasons."));
        }
    }


    // Private helper methods
    /**
     * Retrieves a user from cache or database.
     *
     * @param username Username to look up
     * @return User object if found
     * @throws UserNotFoundException if user doesn't exist
     */
    private User getUserFromCacheOrDatabase(String username) {
        return userCache.get(username, key -> userRepository.findByUsername(key)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + key)));
    }



    /**
     * Logs out the current user and invalidates their authentication tokens.
     *
     * @param token The JWT access token to invalidate
     * @throws AuthenticationException if token is invalid or already invalidated
     * @throws UserNotFoundException if associated user doesn't exist
     */
    @Transactional
    @Override
    public void logout(String token) {
        if (token == null) {
            throw new AuthenticationException("No token provided");
        }

        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        String refreshToken = request.getHeader("Refresh-Token");
        if (refreshToken != null) {
            tokenService.invalidateRefreshToken(refreshToken);
        }

        // Check if token is already blacklisted
        if (tokenService.isTokenBlacklisted(token)) {
            throw new AuthenticationException("Token already invalidated");
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getName() != null) {
            User user = getUserFromCacheOrDatabase(authentication.getName());

            // Validate token belongs to the user
            try {
                String username = jwtTokenProvider.extractUsername(token);
                if (!username.equals(authentication.getName())) {
                    throw new AuthenticationException("Invalid token for current user");
                }
            } catch (Exception e) {
                throw new AuthenticationException("Invalid token");
            }

            // Blacklist the token
            tokenService.blacklistToken(token);

            // Revoke all user tokens of the access token type
            tokenService.revokeAllUserTokens(user, Token.TokenType.ACCESS);

            // Clear security context
            SecurityContextHolder.clearContext();

            // Invalidate user cache
            userCache.invalidate(user.getUsername());

            // Log the user in Audit table
            auditService.logActivity(
                    AuditLog.EntityType.USER,
                    user.getId(),
                    AuditLog.ActionType.LOGOUT,
                    user.getUsername() + "has loggedOut on " + LocalDateTime.now()
            );

            // Publish logout event
            eventPublisher.publishEvent(new UserLogoutEvent(
                    this,
                    user.getId(),
                    loginAttemptService.getClientIP(),
                    request.getSession().getId()
            ));

            log.info("User {} logged out successfully. Token blacklisted.", user.getUsername());
        } else {
            throw new AuthenticationException("No authenticated user found");
        }
    }

    /**
     * Registers a new user in the system.
     *
     * @param registrationDto User registration details including username, email, and password
     * @return UserDto containing the registered user's details
     * @throws UserRegistrationException if username/email already exists or passwords don't match
     */
    @Transactional
    @Override
    public UserDto registerNewUser(UserRegistrationDto registrationDto) {
        // Validate password match
        if (!registrationDto.getPassword().equals(registrationDto.getConfirmPassword())) {
            throw new UserRegistrationException("Passwords do not match");
        }

        // Check if username already exists
        if (userRepository.existsByUsername(registrationDto.getUsername())) {
            throw new UserRegistrationException("Username already exists");
        }

        // Check if email already exists
        if (userRepository.existsByEmail(registrationDto.getEmail())) {
            throw new UserRegistrationException("Email already exists");
        }

        // Create new user
        User newUser = new User();
        newUser.setUsername(registrationDto.getUsername());
        newUser.setEmail(registrationDto.getEmail());
        newUser.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
        newUser.setRole(registrationDto.getRole());
        newUser.setCreatedAt(LocalDateTime.now());

        // Save user
        User savedUser = userRepository.save(newUser);

        // Log the user in Audit table
        auditService.logActivity(
                AuditLog.EntityType.USER,
                newUser.getId(),
                AuditLog.ActionType.NEW_USER,
                newUser.getUsername() + "Registered on " + LocalDateTime.now()
        );

        // Generate email verification token if email verification is enabled
        String verificationToken = tokenService.createEmailVerificationToken(savedUser);

        // Send verification email
        emailService.sendEmailVerificationEmail(savedUser.getEmail(), verificationToken);

        // Convert and return UserDto
        return userMapper.toDto(savedUser);
    }

    /**
     * Checks if the current user is authenticated.
     *
     * @return true if user is authenticated, false otherwise
     */
    @Override
    public boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            return authentication.isAuthenticated();
        }
        return false;
    }

    /**
     * Initiates the password reset process for a user.
     *
     * @param request Contains email address for password reset
     * @throws UserNotFoundException if email doesn't match any user
     */
    @Override
    @Transactional
    public void initiatePasswordReset(ForgotPasswordDto.ForgotPasswordRequest request) {
        // Find user by email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // Generate password reset token using TokenService
        String token = tokenService.createPasswordResetToken(user);

        // Log the user in Audit table
        auditService.logActivity(
                AuditLog.EntityType.USER,
                user.getId(),
                AuditLog.ActionType.PASSWORD_CHANGE,
                user.getUsername() + "has Requested for password change on " + LocalDateTime.now()
        );

        // Send password reset email
        emailService.sendPasswordResetEmail(user.getEmail(), token);

        log.info("Password reset initiated for user: {}", user.getUsername());
    }

    /**
     * Completes the password reset process with a new password.
     *
     * @param request Contains reset token and new password
     * @throws PasswordResetException if token is invalid or password reset fails
     */
    @Override
    @Transactional
    public void resetPassword(ForgotPasswordDto.ResetPasswordRequest request) {
        log.info("Processing password reset request with token");

        if (request.getNewPassword() == null || request.getNewPassword().isEmpty()) {
            throw new PasswordResetException("New password cannot be empty");
        }


        // Validate token and get associated user
        Token resetToken = tokenService.validatePasswordResetToken(request.getToken());
        User user = resetToken.getUser();

        try {
            // Update user's password
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            userRepository.save(user);

            // Invalidate user cache
            userCache.invalidate(user.getUsername());

            // Mark token as used
            tokenService.markTokenAsUsed(resetToken);

            // For security, revoke all refresh tokens for this user
            tokenService.revokeAllUserTokens(user, Token.TokenType.REFRESH);

            // Log the user in Audit table
            auditService.logActivity(
                    AuditLog.EntityType.USER,
                    user.getId(),
                    AuditLog.ActionType.PASSWORD_CHANGE,
                    user.getUsername() + "has Changed Password on " + LocalDateTime.now()
            );

            log.info("Password successfully reset for user: {}", user.getUsername());

            // Optionally send confirmation email
            // emailService.sendPasswordResetConfirmationEmail(user.getEmail());
        } catch (Exception e) {
            log.error("Error resetting password: {}", e.getMessage(), e);
            throw new PasswordResetException("Failed to reset password: " + e.getMessage());
        }
    }

    /**
     * Refreshes the authentication tokens using a valid refresh token.
     *
     * @param refreshToken The refresh token to use
     * @return TokenResponse containing new access and refresh tokens
     * @throws AuthenticationException if refresh token is invalid
     */
    @Override
    @Transactional
    public TokenResponse refreshTokens(String refreshToken) {
        return tokenService.refreshTokens(refreshToken);
    }




    /**
     * Verify email with token
     *
     * @param token The verification token
     * @return true if verification successful, false otherwise
     */
//    @Override
//    @Transactional
//    public boolean verifyEmail(String token) {
//        Token verificationToken = tokenService.findByTokenValue(token, Token.TokenType.EMAIL_VERIFICATION)
//                .orElseThrow(() -> new AuthenticationException("Verification token not found"));
//
//        if (verificationToken.isExpired() || verificationToken.isUsed() || verificationToken.isRevoked()) {
//            return false;
//        }
//
//        User user = verificationToken.getUser();
//        user.setEmailVerified(true);
//        userRepository.save(user);
//
//        // Mark token as used
//        verificationToken.markAsUsed();
//
//        log.info("Email verified for user: {}", user.getUsername());
//        return true;
//    }


    /**
     * Logs cache statistics for monitoring and debugging.
     * Includes hit rate, miss count, and eviction statistics.
     */
    private void logCacheStats() {
        CacheStats userCacheStats = userCache.stats();
        log.info("User Cache Statistics:");
        log.info("Hit count: {}", userCacheStats.hitCount());
        log.info("Miss count: {}", userCacheStats.missCount());
        log.info("Hit rate: {}", String.format("%.2f%%", userCacheStats.hitRate() * 100));
        log.info("Eviction count: {}", userCacheStats.evictionCount());
        log.info("Estimated size: {}", userCache.estimatedSize());

        // Log token service cache stats
        tokenService.logCacheStats();
    }


    // March 26.....

    /**
     * Generates a new OTP for the specified email and purpose.
     *
     * @param email User's email address
     * @param purpose Purpose of OTP (EMAIL_VERIFICATION or PASSWORD_RESET)
     * @return Generated OTP code
     * @throws UserNotFoundException if email doesn't match any user
     */
    @Transactional
    @Override
    public String generateOtp(String email, OtpVerification.OtpPurpose purpose) {
        // Find user by email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        // Delete existing unused OTPs for this user and purpose
        List<OtpVerification> oldOtps = otpRepository.findAll().stream()
                .filter(otp -> otp.getUser().equals(user) &&
                        otp.getPurpose() == purpose &&
                        !otp.isUsed())
                .collect(Collectors.toList());
        otpRepository.deleteAll(oldOtps);

        // Generate new OTP
        String otpCode = RandomStringUtils.randomNumeric(6);

        // Create OTP entity
        OtpVerification otpVerification = OtpVerification.builder()
                .otpCode(otpCode)
                .user(user)
                .purpose(purpose)
                .expiresAt(LocalDateTime.now().plusMinutes(15))
                .build();

        otpRepository.save(otpVerification);

        // Return OTP code (for frontend to handle sending)
        return otpCode;
    }

    /**
     * Sends OTP via email for the specified purpose.
     *
     * @param email Recipient's email address
     * @param purpose Purpose of OTP (EMAIL_VERIFICATION or PASSWORD_RESET)
     * @throws UserNotFoundException if email doesn't match any user
     */
    @Transactional
    @Override
    public void sendOtpEmail(String email,  OtpVerification.OtpPurpose purpose) {
        // Simply delegate to email service
        String otpCode=generateOtp(email,purpose);
        emailService.sendOtpEmail(email, otpCode, purpose);
    }

    /**
     * Verifies an OTP code for the specified purpose.
     *
     * @param otpVerificationDto Contains email and OTP code
     * @param purpose            Purpose of OTP being verified
     * @return true if verification successful
     * @throws InvalidOperationException if OTP is invalid or expired
     * @throws UserNotFoundException     if email doesn't match any user
     */
    @Transactional
    @Override
    public boolean verifyOtp(OtpVerificationDto otpVerificationDto, OtpVerification.OtpPurpose purpose) {
        // Find user by email
        User user = userRepository.findByEmail(otpVerificationDto.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found with email"));

        // Find valid OTP
        Optional<OtpVerification> otpOptional = otpRepository.findValidOtp(
                user,
                otpVerificationDto.getOtpCode(),
                purpose,
                LocalDateTime.now()
        );

        if (otpOptional.isEmpty()) {
            throw new InvalidOperationException("Invalid or expired OTP");
        }

        // Mark OTP as used
        OtpVerification otp = otpOptional.get();
        otp.setUsed(true);
        otpRepository.save(otp);

        // Perform action based on purpose
        switch (purpose) {
            case EMAIL_VERIFICATION:
                user.setEmailVerified(true);
                userRepository.save(user);
                emailService.sendEmailVerificationSuccessEmail(user.getEmail());
                break;
            case PASSWORD_RESET:
                // Additional logic for password reset if needed
                break;
        }

        return true;
    }

}