package com.BookBliss.Events.Authentications;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import java.time.LocalDateTime;
import lombok.ToString;

@Getter
@ToString
public class PasswordResetEvent extends ApplicationEvent {
    private final Long userId;
    private final String email;
    private final LocalDateTime eventTime;
    private final String ipAddress;
    private final PasswordResetType type;

    public enum PasswordResetType {
        REQUESTED,
        COMPLETED,
        FAILED
    }

    public PasswordResetEvent(Object source, Long userId, String email, String ipAddress, PasswordResetType type) {
        super(source);
        this.userId = userId;
        this.email = email;
        this.eventTime = LocalDateTime.now();
        this.ipAddress = ipAddress;
        this.type = type;
    }
}
