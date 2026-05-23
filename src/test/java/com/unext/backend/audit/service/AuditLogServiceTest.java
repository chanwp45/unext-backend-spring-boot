package com.unext.backend.audit.service;

import com.unext.backend.audit.entity.AuditAction;
import com.unext.backend.audit.entity.AuditLog;
import com.unext.backend.audit.repository.AuditLogRepository;
import com.unext.backend.shared.response.PagedData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceTest {

    @Mock private AuditLogRepository auditLogRepository;

    @InjectMocks private AuditLogService auditLogService;

    private AuditContext auditCtx;
    private AuditLog sampleLog;

    @BeforeEach
    void setUp() {
        auditCtx = new AuditContext(
                "user-001", "Admin User", "ADMIN",
                "127.0.0.1", "Mozilla/5.0", "เหตุผลทดสอบ", "DOC-001");

        sampleLog = new AuditLog();
        sampleLog.setTableName("students");
        sampleLog.setRecordId("6701000001");
        sampleLog.setAction(AuditAction.INSERT);
        sampleLog.setOldValues(null);
        sampleLog.setNewValues(Map.of("student_id", "6701000001", "email", "test@example.com"));
        sampleLog.setChangedFields(null);
        sampleLog.setPerformedBy("user-001");
        sampleLog.setPerformedByName("Admin User");
        sampleLog.setUserRole("ADMIN");
        sampleLog.setIpAddress("127.0.0.1");
        sampleLog.setUserAgent("Mozilla/5.0");
        sampleLog.setReason("เหตุผลทดสอบ");
        sampleLog.setDocumentRef("DOC-001");
    }

    // ─── log ────────────────────────────────────────────────────────────────

    @Test
    void log_shouldSaveAuditLog_withAllFields() {
        given(auditLogRepository.save(any(AuditLog.class))).willAnswer(inv -> inv.getArgument(0));

        auditLogService.log(
                "students", "6701000001", AuditAction.INSERT,
                null, Map.of("email", "test@example.com"),
                null, auditCtx);

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        then(auditLogRepository).should().save(captor.capture());

        AuditLog saved = captor.getValue();
        assertThat(saved.getTableName()).isEqualTo("students");
        assertThat(saved.getRecordId()).isEqualTo("6701000001");
        assertThat(saved.getAction()).isEqualTo(AuditAction.INSERT);
        assertThat(saved.getPerformedBy()).isEqualTo("user-001");
        assertThat(saved.getPerformedByName()).isEqualTo("Admin User");
        assertThat(saved.getUserRole()).isEqualTo("ADMIN");
        assertThat(saved.getIpAddress()).isEqualTo("127.0.0.1");
        assertThat(saved.getReason()).isEqualTo("เหตุผลทดสอบ");
        assertThat(saved.getDocumentRef()).isEqualTo("DOC-001");
    }

    @Test
    void log_shouldSaveWithChangedFields_forUpdateAction() {
        List<String> changed = List.of("email", "phone");
        Map<String, Object> old = Map.of("email", "old@example.com");
        Map<String, Object> updated = Map.of("email", "new@example.com");

        given(auditLogRepository.save(any(AuditLog.class))).willAnswer(inv -> inv.getArgument(0));

        auditLogService.log("students", "6701000001", AuditAction.UPDATE, old, updated, changed, auditCtx);

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        then(auditLogRepository).should().save(captor.capture());

        AuditLog saved = captor.getValue();
        assertThat(saved.getAction()).isEqualTo(AuditAction.UPDATE);
        assertThat(saved.getOldValues()).isEqualTo(old);
        assertThat(saved.getNewValues()).isEqualTo(updated);
        assertThat(saved.getChangedFields()).containsExactly("email", "phone");
    }

    @Test
    void log_shouldSaveDeleteAction_withOldValuesOnly() {
        Map<String, Object> old = Map.of("curriculum_code", "CS-2566-01");

        given(auditLogRepository.save(any(AuditLog.class))).willAnswer(inv -> inv.getArgument(0));

        auditLogService.log("curricula", "1", AuditAction.DELETE, old, null, null, auditCtx);

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        then(auditLogRepository).should().save(captor.capture());

        AuditLog saved = captor.getValue();
        assertThat(saved.getAction()).isEqualTo(AuditAction.DELETE);
        assertThat(saved.getOldValues()).isEqualTo(old);
        assertThat(saved.getNewValues()).isNull();
    }

    // ─── getHistory ──────────────────────────────────────────────────────────

    @Test
    void getHistory_shouldReturnPagedAuditLogs_whenRecordExists() {
        var page = new PageImpl<>(List.of(sampleLog), PageRequest.of(0, 20), 1);
        given(auditLogRepository.findByTableNameAndRecordIdOrderByPerformedAtDesc(
                eq("students"), eq("6701000001"), any())).willReturn(page);

        PagedData<AuditLogService.AuditLogEntry> result =
                auditLogService.getHistory("students", "6701000001", 1, 20);

        assertThat(result.items()).hasSize(1);
        assertThat(result.items().get(0).tableName()).isEqualTo("students");
        assertThat(result.items().get(0).recordId()).isEqualTo("6701000001");
        assertThat(result.items().get(0).action()).isEqualTo("INSERT");
        assertThat(result.items().get(0).performedBy()).isEqualTo("user-001");
        assertThat(result.pagination().total()).isEqualTo(1L);
    }

    @Test
    void getHistory_shouldReturnEmptyPage_whenNoLogsExist() {
        var emptyPage = new PageImpl<AuditLog>(List.of(), PageRequest.of(0, 20), 0);
        given(auditLogRepository.findByTableNameAndRecordIdOrderByPerformedAtDesc(
                anyString(), anyString(), any())).willReturn(emptyPage);

        PagedData<AuditLogService.AuditLogEntry> result =
                auditLogService.getHistory("students", "NONEXISTENT", 1, 20);

        assertThat(result.items()).isEmpty();
        assertThat(result.pagination().total()).isZero();
    }

    @Test
    void getHistory_shouldRespectPagination() {
        var page = new PageImpl<>(List.of(sampleLog), PageRequest.of(1, 5), 10);
        given(auditLogRepository.findByTableNameAndRecordIdOrderByPerformedAtDesc(
                anyString(), anyString(), any())).willReturn(page);

        PagedData<AuditLogService.AuditLogEntry> result =
                auditLogService.getHistory("students", "6701000001", 2, 5);

        assertThat(result.pagination().page()).isEqualTo(2);
        assertThat(result.pagination().limit()).isEqualTo(5);
        assertThat(result.pagination().total()).isEqualTo(10L);
    }

    // ─── AuditContext ────────────────────────────────────────────────────────

    @Test
    void auditContext_withReason_shouldOverrideReasonAndDocumentRef() {
        AuditContext original = new AuditContext(
                "user-001", "Admin", "STAFF", "10.0.0.1", "Agent", null, null);

        AuditContext updated = original.withReason("เหตุผลใหม่", "REF-999");

        assertThat(updated.reason()).isEqualTo("เหตุผลใหม่");
        assertThat(updated.documentRef()).isEqualTo("REF-999");
        assertThat(updated.performedBy()).isEqualTo("user-001");
        assertThat(updated.userRole()).isEqualTo("STAFF");
    }
}
