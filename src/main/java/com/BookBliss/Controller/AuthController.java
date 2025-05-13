package com.BookBliss.Controller;

import com.BookBliss.DTO.Auth.*;
import com.BookBliss.DTO.OTP.GenerateOtpRequestDto;
import com.BookBliss.DTO.OTP.OtpVerificationDto;
import com.BookBliss.Entity.OtpVerification;
import com.BookBliss.Exception.AuthenticationException;
import com.BookBliss.Service.Auth.AuthServiceImpl;
import com.BookBliss.Service.Auth.EmailVerificationServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication API for user management, login, registration and password operations")
public class AuthController {

    private final AuthServiceImpl authService;

    private final EmailVerificationServiceImpl emailVerificationService;

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @Operation(
            summary = "User login",
            description = "Authenticates a user and returns access and refresh tokens",
            tags = {"Authentication"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful login",
                    content = @Content(schema = @Schema(implementation = LoginResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(
            @Valid @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Login credentials",
                    required = true,
                    content = @Content(schema = @Schema(implementation = LoginRequestDto.class))
            ) LoginRequestDto loginRequest) {
        log.info("User Logged In");
        return ResponseEntity.ok(authService.login(loginRequest));
    }

    @Operation(
            summary = "User logout",
            description = "Logs out a user by invalidating their token",
            tags = {"Authentication"},
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully logged out"),
            @ApiResponse(responseCode = "401", description = "Unauthorized, invalid token")
    })
    @PostMapping("/logout")
    public ResponseEntity<String> logout(
            @Parameter(description = "JWT token with Bearer prefix", required = true)
            @RequestHeader(value = "Authorization", required = false) String token) {
        log.info("User Logged Out");
        authService.logout(token);
        return ResponseEntity.ok("Logged out successfully");
    }

    @Operation(
            summary = "Register new user",
            description = "Creates a new user account in the system",
            tags = {"Authentication"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User registered successfully",
                    content = @Content(schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid registration data or username/email already exists"),
            @ApiResponse(responseCode = "500", description = "Server error during registration")
    })
    @PostMapping("/register")
    public ResponseEntity<UserDto> registerUser(
            @Valid @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "User registration details",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UserRegistrationDto.class))
            ) UserRegistrationDto registrationDto) {
        UserDto registeredUser = authService.registerNewUser(registrationDto);
        log.info("New User Registered.");
        return ResponseEntity.ok(registeredUser);
    }

    @Operation(
            summary = "Admin dashboard access",
            description = "Protected endpoint accessible only by administrators",
            tags = {"Administration"},
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Admin access granted"),
            @ApiResponse(responseCode = "403", description = "Access denied, requires ADMIN role"),
            @ApiResponse(responseCode = "401", description = "Unauthorized, invalid or missing token")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/dash")
    public ResponseEntity<String> getDash() {
        return ResponseEntity.ok("Admin Dash...") ;
    }

    @Operation(
            summary = "Initiate password reset",
            description = "Sends a password reset link to the user's email",
            tags = {"Authentication"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Password reset email sent"),
            @ApiResponse(responseCode = "404", description = "Email not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(
            @Valid @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Email address for password reset",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ForgotPasswordDto.ForgotPasswordRequest.class))
            ) ForgotPasswordDto.ForgotPasswordRequest request) {
        authService.initiatePasswordReset(request);
        log.info("Forgot Password Requested");
        return ResponseEntity.ok("Password reset link sent to your email");
    }

    @Operation(
            summary = "Complete password reset",
            description = "Resets user password using token received via email",
            tags = {"Authentication"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Password reset successful"),
            @ApiResponse(responseCode = "400", description = "Invalid or expired token"),
            @ApiResponse(responseCode = "500", description = "Server error during password reset")
    })
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(
            @Valid @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "New password and reset token",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ForgotPasswordDto.ResetPasswordRequest.class))
            ) ForgotPasswordDto.ResetPasswordRequest request) {
        authService.resetPassword(request);
        log.info("Password Reset success");
        return ResponseEntity.ok("Password reset successfully");
    }

    @Operation(
            summary = "Refresh authentication tokens",
            description = "Generates new access token using a valid refresh token",
            tags = {"Authentication"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "New tokens generated successfully",
                    content = @Content(schema = @Schema(implementation = TokenResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token"),
            @ApiResponse(responseCode = "500", description = "Server error during token refresh")
    })
    @PostMapping("/refresh-token")
    public ResponseEntity<TokenResponse> refreshToken(
            @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Refresh token",
                    required = true,
                    content = @Content(schema = @Schema(implementation = RefreshTokenRequest.class))
            ) RefreshTokenRequest request) {
        try {
            TokenResponse tokens = authService.refreshTokens(request.getRefreshToken());
            return ResponseEntity.ok(tokens);
        } catch (AuthenticationException e) {
            log.error("Token refresh failed: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error during token refresh: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error processing refresh token");
        }
    }

    // added march 6th

    @Operation(
            summary = "Verify email with token",
            description = "Verifies user email with the provided token",
            tags = {"Authentication"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Email verified successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid or expired token")
    })
    @GetMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestParam String token) {
        log.info("Email verification request");
        boolean verified = emailVerificationService.verifyEmail(token);

        if (verified) {
            return ResponseEntity.ok("Email verified successfully");
        } else {
            return ResponseEntity.badRequest().body("Email verification failed");
        }
    }

    @Operation(
            summary = "Resend verification email",
            description = "Resends verification email to the user",
            tags = {"Authentication"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Verification email sent successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @PostMapping("/resend-verification-email")
    public ResponseEntity<String> resendVerificationEmail(@RequestParam Long userId) {
        log.info("Resend verification email request for user ID: {}", userId);
        String token = emailVerificationService.generateAndSendVerificationToken(userId);
        return ResponseEntity.ok("Verification email sent successfully");
    }

    @Operation(
            summary = "Check email verification status",
            description = "Checks if user's email is verified",
            tags = {"Authentication"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/email-verification-status")
    public ResponseEntity<String> checkEmailVerificationStatus(@RequestParam Long userId) {
        log.info("Email verification status check for user ID: {}", userId);
        boolean isVerified = emailVerificationService.isEmailVerified(userId);
        return ResponseEntity.ok(isVerified ? "Email is verified" : "Email is not verified");
    }

    // March 26 ......
    @Operation(
            summary = "Generate OTP for various purposes",
            description = "Generate and send One-Time Password (OTP) for email verification, password reset, etc.",
            tags = {"Authentication"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OTP generated and sent successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request or user not found"),
            @ApiResponse(responseCode = "500", description = "Server error during OTP generation")
    })
    @PostMapping("/generate-otp")
    public ResponseEntity<String> generateOtp(
            @Valid @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Email and OTP purpose",
                    required = true
            ) GenerateOtpRequestDto request) {
        log.info("Generating OTP for email: {} with purpose: {}", request.getEmail(), request.getPurpose());

        authService.sendOtpEmail(
                request.getEmail(),
                OtpVerification.OtpPurpose.valueOf(request.getPurpose())
        );

        return ResponseEntity.ok("OTP generated and sent successfully");
    }

    @Operation(
            summary = "Verify OTP",
            description = "Verify One-Time Password for various purposes",
            tags = {"Authentication"}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OTP verified successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid or expired OTP"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PostMapping("/verify-otp")
    public ResponseEntity<String> verifyOtp(
            @Valid @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "OTP verification details",
                    required = true,
                    content = @Content(schema = @Schema(implementation = OtpVerificationDto.class))
            ) OtpVerificationDto otpVerificationDto,
            @RequestParam String purpose) {
        log.info("Verifying OTP for email: {} with purpose: {}", otpVerificationDto.getEmail(), purpose);

        boolean verified = authService.verifyOtp(
                otpVerificationDto,
                OtpVerification.OtpPurpose.valueOf(purpose)
        );

        return ResponseEntity.ok("OTP verified successfully");
    }

}