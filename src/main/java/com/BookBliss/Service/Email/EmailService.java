package com.BookBliss.Service.Email;


import com.BookBliss.Entity.OtpVerification;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public interface EmailService {
    /**
     * Send password reset email
     *
     * @param to Email recipient
     * @param token Reset token
     */
    void sendPasswordResetEmail(String to, String token);

    /**
     * Send email verification email
     *
     * @param email Email recipient
     * @param verificationToken Verification token
     */
    void sendEmailVerificationEmail(@NotBlank(message = "Email is required") @Email(message = "Invalid email format") String email, String verificationToken);

    /**
     * Send email verification success notification
     *
     * @param email Email recipient
     */
    void sendEmailVerificationSuccessEmail(String email);

    /**
     * Send email verification reminder
     *
     * @param email Email recipient
     * @param verificationToken Verification token
     */
    void sendEmailVerificationReminder(String email, String verificationToken);


    // Two-Factor Authentication Methods
    void sendOtpEmail(String email, String otpCode, OtpVerification.OtpPurpose purpose);

    void sendTwoFactorLoginOtp(String email, String otpCode);
    void sendTwoFactorEnabledNotification(String email, String method);
    void sendTwoFactorDisabledNotification(String email);

    // Security Alert Methods
    void sendSecurityAlertEmail(String email, String alertType);
}