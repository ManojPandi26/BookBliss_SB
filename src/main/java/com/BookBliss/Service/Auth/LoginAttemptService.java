package com.BookBliss.Service.Auth;

import com.BookBliss.Events.Authentications.AccountLockEvent;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginAttemptService {
    private final ApplicationEventPublisher eventPublisher;
    private final HttpServletRequest request;

    @Value("${security.max-login-attempts:5}")
    private int maxAttempts;

    @Value("${security.block-duration-minutes:30}")
    private int blockDurationMinutes;

    private final Cache<String, LoginAttemptInfo> attemptsCache = Caffeine.newBuilder()
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .build();

    public void loginSucceeded(String key) {
        LoginAttemptInfo info = attemptsCache.getIfPresent(key);
        if (info != null) {
            info.resetAttempts();
            attemptsCache.put(key, info);
            log.debug("Login succeeded for IP: {}. Attempts reset.", key);
        }
    }

    public void loginFailed(String key, Long userId) {
        LoginAttemptInfo info = attemptsCache.get(key, k -> new LoginAttemptInfo());
        info.incrementAttempts();

        if (info.getAttempts() >= maxAttempts) {
            info.setBlockedUntil(LocalDateTime.now().plusMinutes(blockDurationMinutes));

            // Publish account lock event
            eventPublisher.publishEvent(new AccountLockEvent(
                    this,
                    userId,
                    key,
                    AccountLockEvent.LockReason.FAILED_ATTEMPTS,
                    (long) blockDurationMinutes
            ));

            log.warn("Account locked for IP: {} due to {} failed attempts", key, maxAttempts);
        }

        attemptsCache.put(key, info);
        log.debug("Login failed for IP: {}. Attempt #{}", key, info.getAttempts());
    }

    public boolean isBlocked(String key) {
        LoginAttemptInfo info = attemptsCache.getIfPresent(key);
        if (info != null && info.getBlockedUntil() != null) {
            if (LocalDateTime.now().isAfter(info.getBlockedUntil())) {
                info.resetAttempts();
                attemptsCache.put(key, info);
                return false;
            }
            return true;
        }
        return false;
    }

    public int getRemainingAttempts(String key) {
        LoginAttemptInfo info = attemptsCache.getIfPresent(key);
        if (info == null) {
            return maxAttempts;
        }
        return Math.max(0, maxAttempts - info.getAttempts());
    }

    public LocalDateTime getBlockedUntil(String key) {
        LoginAttemptInfo info = attemptsCache.getIfPresent(key);
        return info != null ? info.getBlockedUntil() : null;
    }

    public void clearAttempts(String key) {
        attemptsCache.invalidate(key);
        log.debug("Cleared login attempts for IP: {}", key);
    }

    public String getClientIP() {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty()) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }
}

// Helper class to store attempt information
@Getter
class LoginAttemptInfo {
    private int attempts;
    @Setter
    private LocalDateTime blockedUntil;
    private final LocalDateTime createdAt;

    public LoginAttemptInfo() {
        this.attempts = 0;
        this.blockedUntil = null;
        this.createdAt = LocalDateTime.now();
    }

    public void incrementAttempts() {
        this.attempts++;
    }

    public void resetAttempts() {
        this.attempts = 0;
        this.blockedUntil = null;
    }

}