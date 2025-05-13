package com.BookBliss.Service.Auth;

import com.BookBliss.Entity.Token;
import com.BookBliss.Entity.User;
import com.BookBliss.Exception.UserNotFoundException;
import com.BookBliss.Repository.TokenRepository;
import com.BookBliss.Repository.UserRepository;
import com.BookBliss.Service.Email.EmailService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmailVerificationServiceImpl implements EmailVerificationService {
    private static final Logger log = LoggerFactory.getLogger(EmailVerificationServiceImpl.class);

    private final TokenService tokenService;
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;

    @Override
    @Transactional
    public String generateAndSendVerificationToken(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        log.info("Generating verification token for user: {}", user.getUsername());

        // Check if there's already an active token
        List<Token> existingTokens = tokenRepository.findValidTokensByUser(
                user, Token.TokenType.EMAIL_VERIFICATION, LocalDateTime.now());

        // Revoke existing tokens
        if (!existingTokens.isEmpty()) {
            existingTokens.forEach(token -> {
                token.markAsRevoked();
                tokenRepository.save(token);
                log.debug("Revoked existing verification token for user: {}", user.getUsername());
            });
        }

        // Generate new token
        String verificationToken = tokenService.createEmailVerificationToken(user);

        // Send verification email
        emailService.sendEmailVerificationEmail(user.getEmail(), verificationToken);

        return verificationToken;
    }

    @Override
    @Transactional
    public boolean verifyEmail(String token) {
        log.info("Processing email verification with token");

        try {
            Token verificationToken = tokenService.validateEmailVerificationToken(token);
            User user = verificationToken.getUser();

            // Skip if already verified
            if (user.isEmailVerified()) {
                log.info("Email already verified for user: {}", user.getUsername());
                return true;
            }

            // Mark email as verified
            user.setEmailVerified(true);
            userRepository.save(user);

            // Mark token as used
            tokenService.markTokenAsUsed(verificationToken);

            // Send success notification
            emailService.sendEmailVerificationSuccessEmail(user.getEmail());

            log.info("Email successfully verified for user: {}", user.getUsername());
            return true;
        } catch (Exception e) {
            log.error("Email verification failed: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean isEmailVerified(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
        return user.isEmailVerified();
    }

    @Override
    @Transactional
    public int sendVerificationReminders(int daysSinceRegistration) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysSinceRegistration);

        List<User> unverifiedUsers = userRepository.findByEmailVerifiedFalseAndCreatedAtBefore(cutoffDate);
        int reminderCount = 0;

        for (User user : unverifiedUsers) {
            try {
                // Find if there's a valid token
                List<Token> validTokens = tokenRepository.findValidTokensByUser(
                        user, Token.TokenType.EMAIL_VERIFICATION, LocalDateTime.now());

                String verificationToken;
                if (validTokens.isEmpty()) {
                    // Create new token if none exists
                    verificationToken = tokenService.createEmailVerificationToken(user);
                } else {
                    // Use existing token
                    verificationToken = validTokens.get(0).getTokenValue();
                }

                // Send reminder
                emailService.sendEmailVerificationReminder(user.getEmail(), verificationToken);
                reminderCount++;

                log.info("Sent verification reminder to user: {}", user.getUsername());
            } catch (Exception e) {
                log.error("Failed to send verification reminder to user {}: {}",
                        user.getUsername(), e.getMessage(), e);
            }
        }

        log.info("Sent {} verification reminders", reminderCount);
        return reminderCount;
    }

    @Scheduled(cron = "0 0 9 * * ?") // Run at 9 AM every day
    @Transactional
    public void scheduledVerificationReminders() {
        log.info("Running scheduled verification reminders");
        // Send reminders to users who registered 2 days ago but haven't verified email
        sendVerificationReminders(2);
    }
}
