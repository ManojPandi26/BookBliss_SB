package com.BookBliss.Events.Authentications;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import java.time.LocalDateTime;
import lombok.ToString;

@Getter
@ToString
public class AccountLockEvent extends ApplicationEvent {
    private final Long userId;
    private final String ipAddress;
    private final LocalDateTime eventTime;
    private final LockReason reason;
    private final Long lockDuration;

    public enum LockReason {
        FAILED_ATTEMPTS,
        SUSPICIOUS_ACTIVITY,
        ADMIN_ACTION
    }

    public AccountLockEvent(Object source, Long userId, String ipAddress, LockReason reason, Long lockDuration) {
        super(source);
        this.userId = userId;
        this.ipAddress = ipAddress;
        this.eventTime = LocalDateTime.now();
        this.reason = reason;
        this.lockDuration = lockDuration;
    }
}