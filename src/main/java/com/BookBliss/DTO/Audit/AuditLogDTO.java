package com.BookBliss.DTO.Audit;

import com.BookBliss.Entity.AuditLog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogDTO {
    private Long id;
    private AuditLog.EntityType entityType;
    private Long entityId;
    private AuditLog.ActionType action;
    private String details;
    private Long actorId;
    private String actorName;
    private LocalDateTime timestamp;
    private String ipAddress;
}
