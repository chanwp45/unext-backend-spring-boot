package com.unext.backend.audit.service;

import com.unext.backend.audit.entity.AuditAction;
import com.unext.backend.audit.entity.AuditLog;
import com.unext.backend.audit.repository.AuditLogRepository;
import com.unext.backend.shared.response.PagedData;
import com.unext.backend.shared.response.PaginationMeta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * Records an audit event. Runs in a separate transaction so a business-layer
     * rollback does not suppress the audit trail.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String tableName, String recordId, AuditAction action,
                    Map<String, Object> oldValues, Map<String, Object> newValues,
                    List<String> changedFields, AuditContext ctx) {
        AuditLog log = new AuditLog();
        log.setTableName(tableName);
        log.setRecordId(recordId);
        log.setAction(action);
        log.setOldValues(oldValues);
        log.setNewValues(newValues);
        log.setChangedFields(changedFields);
        log.setPerformedBy(ctx.performedBy());
        log.setPerformedByName(ctx.performedByName());
        log.setUserRole(ctx.userRole());
        log.setIpAddress(ctx.ipAddress());
        log.setUserAgent(ctx.userAgent());
        log.setReason(ctx.reason());
        log.setDocumentRef(ctx.documentRef());
        auditLogRepository.save(log);
    }

    /** Returns paginated audit history for a specific record. */
    @Transactional(readOnly = true)
    public PagedData<AuditLogEntry> getHistory(String tableName, String recordId, int page, int limit) {
        Page<AuditLog> result = auditLogRepository
                .findByTableNameAndRecordIdOrderByPerformedAtDesc(
                        tableName, recordId, PageRequest.of(page - 1, limit));
        List<AuditLogEntry> items = result.getContent().stream().map(AuditLogEntry::from).toList();
        return new PagedData<>(items, PaginationMeta.of(page, limit, result.getTotalElements()));
    }

    public record AuditLogEntry(
            Long auditId,
            String tableName,
            String recordId,
            String action,
            Object oldValues,
            Object newValues,
            List<String> changedFields,
            String performedBy,
            String performedByName,
            String userRole,
            String ipAddress,
            String reason,
            String documentRef,
            String performedAt
    ) {
        public static AuditLogEntry from(AuditLog log) {
            return new AuditLogEntry(
                    log.getAuditId(), log.getTableName(), log.getRecordId(),
                    log.getAction().name(), log.getOldValues(), log.getNewValues(),
                    log.getChangedFields(), log.getPerformedBy(), log.getPerformedByName(),
                    log.getUserRole(), log.getIpAddress(), log.getReason(), log.getDocumentRef(),
                    log.getPerformedAt().toString());
        }
    }
}
