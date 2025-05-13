package com.BookBliss.Controller;

import com.BookBliss.Entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.BookBliss.DTO.Audit.AuditLogDTO;
import com.BookBliss.DTO.Audit.AuditLogSearchCriteria;
import com.BookBliss.Service.Audit.AuditService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/audit-logs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AuditLogController {

    private final AuditService auditService;

    @GetMapping
    public ResponseEntity<Page<AuditLogDTO>> getSystemAuditLogs(
            @RequestBody AuditLogSearchCriteria criteria,
            @PageableDefault(size = 20, sort = "timestamp") Pageable pageable) {
        return ResponseEntity.ok(auditService.getSystemAuditLogs(criteria, pageable));
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<Page<AuditLogDTO>> getUserAuditLogs(
            @PathVariable Long userId,
            @PageableDefault(size = 20, sort = "timestamp") Pageable pageable) {
        return ResponseEntity.ok(auditService.getUserAuditLogs(userId, pageable));
    }

    @GetMapping("/{entityType}/{entityId}")
    public ResponseEntity<Page<AuditLogDTO>> getEntityAuditLogs(
            @PathVariable AuditLog.EntityType entityType,
            @PathVariable Long entityId,
            @PageableDefault(size = 20, sort = "timestamp") Pageable pageable) {
        return ResponseEntity.ok(auditService.getEntityAuditLogs(entityType, entityId, pageable));
    }
}
