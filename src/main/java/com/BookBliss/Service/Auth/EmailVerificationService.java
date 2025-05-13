package com.BookBliss.Service.Auth;

public interface EmailVerificationService {
    /**
     * Generate and send a new verification token for a user
     *
     * @param user User who needs verification
     * @return The token value
     */
    String generateAndSendVerificationToken(Long userId);

    /**
     * Verify email with token
     *
     * @param token Verification token
     * @return true if verification successful, false otherwise
     */
    boolean verifyEmail(String token);

    /**
     * Check if user's email is verified
     *
     * @param userId User ID
     * @return true if verified, false otherwise
     */
    boolean isEmailVerified(Long userId);

    /**
     * Send verification reminder to unverified users
     *
     * @param daysSinceRegistration Days since user registered
     * @return Number of reminders sent
     */
    int sendVerificationReminders(int daysSinceRegistration);
}