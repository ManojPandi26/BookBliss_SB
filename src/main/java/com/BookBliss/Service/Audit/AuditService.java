package com.BookBliss.Service.Audit;

import com.BookBliss.DTO.Audit.AuditLogDTO;
import com.BookBliss.DTO.Audit.AuditLogSearchCriteria;
import com.BookBliss.Entity.AuditLog;
import com.BookBliss.Entity.User;
import com.BookBliss.Exception.UserNotFoundException;
import com.BookBliss.Mapper.AuditLogMapper;
import com.BookBliss.Repository.AuditLogRepository;
import com.BookBliss.Repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;


@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final AuditLogMapper auditLogMapper;

    public void logActivity(AuditLog.EntityType entityType, Long entityId, AuditLog.ActionType action, String details) {
        try {
            // Get the current authenticated user

            String actorName = "System";
            Long actorId=null;
            if (entityId!=null) {
                User user=userRepository.findById(
                        entityId)
                        .orElseThrow(()-> new UserNotFoundException("User Not Found with Id:"+entityId)) ;
                actorId = user.getId();
                actorName = user.getUsername();
            }

            // Get the IP address from the request
            String ipAddress = getClientIpAddress();

            // Create and save the audit log
            AuditLog auditLog = AuditLog.builder()
                    .entityType(entityType)
                    .entityId(entityId)
                    .action(action)
                    .details(details)
                    .actorId(actorId)
                    .actorName(actorName)
                    .ipAddress(ipAddress)
                    .build();

            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.error("Failed to log audit activity: {}", e.getMessage(), e);
        }
    }

    private String getClientIpAddress() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String xForwardedForHeader = request.getHeader("X-Forwarded-For");
                if (xForwardedForHeader != null && !xForwardedForHeader.isEmpty()) {
                    return xForwardedForHeader.split(",")[0].trim();
                }
                return request.getRemoteAddr();
            }
        } catch (Exception e) {
            log.warn("Could not determine client IP address: {}", e.getMessage());
        }
        return "unknown";
    }

    public Page<AuditLogDTO> getUserAuditLogs(Long userId, Pageable pageable) {
        Page<AuditLog> logs = auditLogRepository.findByEntityTypeAndEntityId(
                AuditLog.EntityType.USER, userId, pageable);
        return logs.map(auditLogMapper::toDto);
    }

    public Page<AuditLogDTO> getSystemAuditLogs(AuditLogSearchCriteria criteria, Pageable pageable) {
        Specification<AuditLog> spec = buildSpecification(criteria);
        Page<AuditLog> logs = auditLogRepository.findAll(spec, pageable);
        return logs.map(auditLogMapper::toDto);
    }

    private Specification<AuditLog> buildSpecification(AuditLogSearchCriteria criteria) {
        // Implementation of dynamic specification builder based on search criteria
        // This is a placeholder and would need to be implemented with actual logic
        return (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();
    }

    public Page<AuditLogDTO> getEntityAuditLogs(AuditLog.EntityType entityType, Long entityId, Pageable pageable) {
        Page<AuditLog> logs = auditLogRepository.findByEntityTypeAndEntityId(entityType, entityId, pageable);
        return logs.map(auditLogMapper::toDto);
    }
}