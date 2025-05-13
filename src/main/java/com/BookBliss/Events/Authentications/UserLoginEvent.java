package com.BookBliss.Events.Authentications;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import java.time.LocalDateTime;
import lombok.ToString;

@Getter
@ToString
public class UserLoginEvent extends ApplicationEvent {
    private final Long userId;
    private final String ipAddress;
    private final LocalDateTime eventTime;
    private final String userAgent;
    private final boolean success;

    public UserLoginEvent(Object source, Long userId, String ipAddress, String userAgent, boolean success) {
        super(source);
        this.userId = userId;
        this.ipAddress = ipAddress;
        this.eventTime = LocalDateTime.now();
        this.userAgent = userAgent;
        this.success = success;
    }
}
