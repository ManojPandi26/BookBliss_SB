package com.BookBliss.DTO.Audit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogSearchCriteria {
    private String entityType;
    private Long entityId;
    private String action;
    private Long actorId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
