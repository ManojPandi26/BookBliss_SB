package com.BookBliss.Mapper;

import com.BookBliss.DTO.Audit.AuditLogDTO;
import com.BookBliss.Entity.AuditLog;
import org.springframework.stereotype.Component;

@Component
public class AuditLogMapper {
    public AuditLogDTO toDto(AuditLog auditLog){
        return AuditLogDTO.builder()
                .id(auditLog.getId())
                .action(auditLog.getAction())
                .actorId(auditLog.getActorId())
                .entityId(auditLog.getEntityId())
                .entityType(auditLog.getEntityType())
                .details(auditLog.getDetails())
                .actorName(auditLog.getActorName())
                .timestamp(auditLog.getTimestamp())
                .ipAddress(auditLog.getIpAddress())
                .build();
    }
}
