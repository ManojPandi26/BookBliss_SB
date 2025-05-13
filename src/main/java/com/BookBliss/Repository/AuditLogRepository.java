package com.BookBliss.Repository;

import com.BookBliss.Entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    Page<AuditLog> findByEntityTypeAndEntityId(AuditLog.EntityType entityType, Long entityId, Pageable pageable);

    Page<AuditLog> findByActorId(Long actorId, Pageable pageable);

    Page<AuditLog> findByAction(String action, Pageable pageable);

    Page<AuditLog> findByTimestampBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);

    @Query("SELECT a FROM AuditLog a WHERE a.entityType = :entityType AND a.action = :action")
    Page<AuditLog> findByEntityTypeAndAction(
            @Param("entityType") String entityType,
            @Param("action") String action,
            Pageable pageable);

    @Query("SELECT a FROM AuditLog a WHERE a.entityType = :entityType AND a.entityId = :entityId AND a.action = :action")
    Page<AuditLog> findByEntityTypeAndEntityIdAndAction(
            @Param("entityType") String entityType,
            @Param("entityId") Long entityId,
            @Param("action") String action,
            Pageable pageable);

    @Query("SELECT a FROM AuditLog a WHERE a.actorId = :actorId AND a.action = :action")
    Page<AuditLog> findByActorIdAndAction(
            @Param("actorId") Long actorId,
            @Param("action") String action,
            Pageable pageable);

    Page<AuditLog> findAll(Specification<AuditLog> spec, Pageable pageable);
}
