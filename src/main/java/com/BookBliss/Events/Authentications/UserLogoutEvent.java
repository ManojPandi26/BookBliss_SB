package com.BookBliss.Events.Authentications;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import java.time.LocalDateTime;
import lombok.ToString;

@Getter
@ToString
public class UserLogoutEvent extends ApplicationEvent {
    private final Long userId;
    private final String ipAddress;
    private final LocalDateTime eventTime;
    private final String sessionId;

    public UserLogoutEvent(Object source, Long userId, String ipAddress, String sessionId) {
        super(source);
        this.userId = userId;
        this.ipAddress = ipAddress;
        this.eventTime = LocalDateTime.now();
        this.sessionId = sessionId;
    }
}