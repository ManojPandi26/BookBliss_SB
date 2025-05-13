package com.BookBliss.Service.Email;

import com.BookBliss.Entity.OtpVerification;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final JavaMailSender mailSender;

    @Value("${app.frontend-base-url:http://localhost:5173}")
    private String frontendBaseUrl;

    @Value("${app.email.from:noreply@BookBliss.com}")
    private String emailFrom;

    @Value("${app.email.verification-token-expiry-hours:24}")
    private int verificationTokenExpiryHours;

    @Async
    @Override
    public void sendPasswordResetEmail(String to, String token) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(emailFrom);
            message.setTo(to);
            message.setSubject("Password Reset Request");

            String resetUrl = frontendBaseUrl + "/reset-password?token=" + token;
            message.setText("To reset your password, click the link below:\n" + resetUrl +
                    "\n\nThis link will expire in 1 hour. If you did not request a password reset, please ignore this email.");

            mailSender.send(message);
            log.info("Password reset email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send password reset email: {}", e.getMessage(), e);
        }
    }

    @Async
    @Override
    public void sendEmailVerificationEmail(@NotBlank(message = "Email is required") @Email(message = "Invalid email format") String email, String verificationToken) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(emailFrom);
            message.setTo(email);
            message.setSubject("Email Verification - BookBliss");

            String verificationUrl = frontendBaseUrl + "/verify-email?token=" + verificationToken;

            StringBuilder emailText = new StringBuilder();
            emailText.append("Hello,\n\n");
            emailText.append("Thank you for registering with BookBliss. Please verify your email address by clicking the link below:\n\n");
            emailText.append(verificationUrl).append("\n\n");
            emailText.append("This link will expire in " + verificationTokenExpiryHours + " hours.\n\n");
            emailText.append("If you did not create an account, please ignore this email.\n\n");
            emailText.append("Regards,\nThe BookBliss Team");

            message.setText(emailText.toString());

            mailSender.send(message);
            log.info("Verification email sent to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send verification email: {}", e.getMessage(), e);
        }
    }

    @Async
    @Override
    public void sendEmailVerificationSuccessEmail(String email) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(emailFrom);
            message.setTo(email);
            message.setSubject("Email Verification Successful - BookBliss");

            StringBuilder emailText = new StringBuilder();
            emailText.append("Hello,\n\n");
            emailText.append("Your email has been successfully verified. Thank you for completing the registration process.\n\n");
            emailText.append("You can now access all features of BookBliss.\n\n");
            emailText.append("Regards,\nThe BookBliss Team");

            message.setText(emailText.toString());

            mailSender.send(message);
            log.info("Verification success email sent to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send verification success email: {}", e.getMessage(), e);
        }
    }

    @Async
    @Override
    public void sendEmailVerificationReminder(String email, String verificationToken) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(emailFrom);
            message.setTo(email);
            message.setSubject("Reminder: Verify Your Email - BookBliss");

            String verificationUrl = frontendBaseUrl + "/verify-email?token=" + verificationToken;

            StringBuilder emailText = new StringBuilder();
            emailText.append("Hello,\n\n");
            emailText.append("This is a reminder to verify your email address for your BookBliss account.\n\n");
            emailText.append("Please click the link below to verify your email:\n\n");
            emailText.append(verificationUrl).append("\n\n");
            emailText.append("This link will expire in " + verificationTokenExpiryHours + " hours.\n\n");
            emailText.append("If you did not create an account, please ignore this email.\n\n");
            emailText.append("Regards,\nThe BookBliss Team");

            message.setText(emailText.toString());

            mailSender.send(message);
            log.info("Verification reminder email sent to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send verification reminder email: {}", e.getMessage(), e);
        }
    }

    // March 26

    @Async
    @Override
    public void sendTwoFactorLoginOtp(String email, String otpCode) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(emailFrom);
            message.setTo(email);
            message.setSubject("Your BookBliss Two-Factor Authentication Code");

            StringBuilder emailText = new StringBuilder();
            emailText.append("Hello,\n\n");
            emailText.append("Your two-factor authentication code is: ").append(otpCode).append("\n\n");
            emailText.append("This code will expire in 15 minutes.\n\n");
            emailText.append("If you did not attempt to log in, please secure your account immediately.\n\n");
            emailText.append("Regards,\nThe BookBliss Team");

            message.setText(emailText.toString());

            mailSender.send(message);
            log.info("Two-factor login OTP sent to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send two-factor login OTP: {}", e.getMessage(), e);
        }
    }

    @Async
    @Override
    public void sendTwoFactorEnabledNotification(String email, String method) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(emailFrom);
            message.setTo(email);
            message.setSubject("Two-Factor Authentication Enabled - BookBliss");

            StringBuilder emailText = new StringBuilder();
            emailText.append("Hello,\n\n");
            emailText.append("Two-factor authentication has been enabled for your BookBliss account.\n\n");
            emailText.append("Authentication Method: ").append(method).append("\n\n");
            emailText.append("If you did not make this change, please contact our support team immediately.\n\n");
            emailText.append("Regards,\nThe BookBliss Team");

            message.setText(emailText.toString());

            mailSender.send(message);
            log.info("Two-factor enabled notification sent to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send two-factor enabled notification: {}", e.getMessage(), e);
        }
    }

    @Async
    @Override
    public void sendTwoFactorDisabledNotification(String email) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(emailFrom);
            message.setTo(email);
            message.setSubject("Two-Factor Authentication Disabled - BookBliss");

            StringBuilder emailText = new StringBuilder();
            emailText.append("Hello,\n\n");
            emailText.append("Two-factor authentication has been disabled for your BookBliss account.\n\n");
            emailText.append("If you did not make this change, please contact our support team immediately.\n\n");
            emailText.append("Regards,\nThe BookBliss Team");

            message.setText(emailText.toString());

            mailSender.send(message);
            log.info("Two-factor disabled notification sent to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send two-factor disabled notification: {}", e.getMessage(), e);
        }
    }

    @Async
    @Override
    public void sendSecurityAlertEmail(String email, String alertType) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(emailFrom);
            message.setTo(email);
            message.setSubject("Security Alert - BookBliss");

            StringBuilder emailText = new StringBuilder();
            emailText.append("Hello,\n\n");
            emailText.append("We detected a ").append(alertType).append(" on your BookBliss account.\n\n");

            switch (alertType) {
                case "LOGIN_FROM_NEW_DEVICE":
                    emailText.append("A new device has been used to log into your account.\n");
                    break;
                case "PASSWORD_CHANGE":
                    emailText.append("Your account password was recently changed.\n");
                    break;
                case "SUSPICIOUS_ACTIVITY":
                    emailText.append("We detected some suspicious activity on your account.\n");
                    break;
            }

            emailText.append("If this was not you, please contact our support team immediately.\n\n");
            emailText.append("Regards,\nThe BookBliss Team");

            message.setText(emailText.toString());

            mailSender.send(message);
            log.info("Security alert email sent to: {} for type: {}", email, alertType);
        } catch (Exception e) {
            log.error("Failed to send security alert email: {}", e.getMessage(), e);
        }
    }
    @Async
    @Override
    public void sendOtpEmail(
            @NotBlank(message = "Email is required")
            @Email(message = "Invalid email format")
            String email,
            String otpCode,
            OtpVerification.OtpPurpose purpose
    ) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(emailFrom);
            message.setTo(email);

            // Customize email based on OTP purpose
            switch (purpose) {
                case EMAIL_VERIFICATION:
                    message.setSubject("Email Verification OTP - BookBliss");
                    message.setText(buildEmailVerificationOtpMessage(otpCode));
                    break;

                case PASSWORD_RESET:
                    message.setSubject("Password Reset OTP - BookBliss");
                    message.setText(buildPasswordResetOtpMessage(otpCode));
                    break;

                case LOGIN_VERIFICATION:
                    message.setSubject("Login Verification OTP - BookBliss");
                    message.setText(buildLoginVerificationOtpMessage(otpCode));
                    break;

                case ACCOUNT_RECOVERY:
                    message.setSubject("Account Recovery OTP - BookBliss");
                    message.setText(buildAccountRecoveryOtpMessage(otpCode));
                    break;

                default:
                    throw new IllegalArgumentException("Unsupported OTP purpose");
            }

            mailSender.send(message);
            log.info("OTP email sent to: {} for purpose: {}", email, purpose);
        } catch (Exception e) {
            log.error("Failed to send OTP email: {}", e.getMessage(), e);
        }
    }

    // Helper methods to build OTP email messages
    private String buildEmailVerificationOtpMessage(String otpCode) {
        return String.format(
                "Hello,\n\n" +
                        "Your email verification OTP is: %s\n\n" +
                        "This OTP will expire in 15 minutes.\n\n" +
                        "If you did not request this verification, please ignore this email.\n\n" +
                        "Regards,\nThe BookBliss Team",
                otpCode
        );
    }

    private String buildPasswordResetOtpMessage(String otpCode) {
        return String.format(
                "Hello,\n\n" +
                        "Your password reset OTP is: %s\n\n" +
                        "This OTP will expire in 15 minutes.\n\n" +
                        "If you did not request a password reset, please contact support.\n\n" +
                        "Regards,\nThe BookBliss Team",
                otpCode
        );
    }

    private String buildLoginVerificationOtpMessage(String otpCode) {
        return String.format(
                "Hello,\n\n" +
                        "Your login verification OTP is: %s\n\n" +
                        "This OTP will expire in 15 minutes.\n\n" +
                        "If you did not attempt to log in, please change your password.\n\n" +
                        "Regards,\nThe BookBliss Team",
                otpCode
        );
    }

    private String buildAccountRecoveryOtpMessage(String otpCode) {
        return String.format(
                "Hello,\n\n" +
                        "Your account recovery OTP is: %s\n\n" +
                        "This OTP will expire in 15 minutes.\n\n" +
                        "If you did not initiate account recovery, please contact support.\n\n" +
                        "Regards,\nThe BookBliss Team",
                otpCode
        );
    }
}