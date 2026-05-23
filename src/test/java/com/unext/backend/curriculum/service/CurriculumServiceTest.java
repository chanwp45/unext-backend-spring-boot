package com.unext.backend.curriculum.service;

import com.unext.backend.audit.entity.AuditAction;
import com.unext.backend.audit.service.AuditContext;
import com.unext.backend.audit.service.AuditLogService;
import com.unext.backend.curriculum.dto.CreateCurriculumRequest;
import com.unext.backend.curriculum.dto.CurriculumResponse;
import com.unext.backend.curriculum.dto.UpdateCurriculumRequest;
import com.unext.backend.curriculum.entity.Curriculum;
import com.unext.backend.curriculum.entity.CurriculumStatus;
import com.unext.backend.curriculum.exception.CurriculumNotFoundException;
import com.unext.backend.curriculum.repository.CurriculumRepository;
import com.unext.backend.faculty.entity.Department;
import com.unext.backend.faculty.entity.Faculty;
import com.unext.backend.faculty.repository.DepartmentRepository;
import com.unext.backend.faculty.repository.FacultyRepository;
import com.unext.backend.shared.exception.AppException;
import com.unext.backend.shared.response.PagedData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class CurriculumServiceTest {

    @Mock private CurriculumRepository curriculumRepository;
    @Mock private FacultyRepository facultyRepository;
    @Mock private DepartmentRepository departmentRepository;
    @Mock private AuditLogService auditLogService;

    @InjectMocks private CurriculumService curriculumService;

    private AuditContext auditCtx;
    private Faculty faculty;
    private Department department;
    private Curriculum existingCurriculum;

    @BeforeEach
    void setUp() {
        auditCtx = new AuditContext("admin", "Admin User", "STAFF", "127.0.0.1", "TestAgent", null, null);

        faculty = new Faculty();
        faculty.setCode("ENG");
        faculty.setNameTh("คณะวิศวกรรมศาสตร์");
        faculty.setNameEn("Faculty of Engineering");

        department = new Department();
        department.setCode("CS");
        department.setNameTh("สาขาวิทยาการคอมพิวเตอร์");
        department.setNameEn("Computer Science");
        department.setFaculty(faculty);

        existingCurriculum = new Curriculum();
        existingCurriculum.setCurriculumCode("CS-2566-01");
        existingCurriculum.setCurriculumNameTh("วิทยาการคอมพิวเตอร์");
        existingCurriculum.setCurriculumNameEn("Computer Science");
        existingCurriculum.setDegreeLevel("Bachelor");
        existingCurriculum.setFaculty(faculty);
        existingCurriculum.setDepartment(department);
        existingCurriculum.setTotalCredits(120);
        existingCurriculum.setDurationYears(new BigDecimal("4.0"));
        existingCurriculum.setEffectiveYear(2566);
        existingCurriculum.setStatus(CurriculumStatus.ACTIVE);
        existingCurriculum.setCreatedBy("admin");
        setId(existingCurriculum, 1L);
    }

    private static void setId(Curriculum c, Long id) {
        try {
            Field f = Curriculum.class.getDeclaredField("id");
            f.setAccessible(true);
            f.set(c, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // ─── findById ───────────────────────────────────────────────────────────

    @Test
    void findById_shouldReturnCurriculum_whenExists() {
        // Arrange
        given(curriculumRepository.findById(1L)).willReturn(Optional.of(existingCurriculum));

        // Act
        CurriculumResponse result = curriculumService.findById(1L);

        // Assert
        assertThat(result.curriculumCode()).isEqualTo("CS-2566-01");
        assertThat(result.degreeLevel()).isEqualTo("Bachelor");
    }

    @Test
    void findById_shouldThrowNotFound_whenCurriculumMissing() {
        // Arrange
        given(curriculumRepository.findById(anyLong())).willReturn(Optional.empty());

        // Act / Assert
        assertThatThrownBy(() -> curriculumService.findById(999L))
                .isInstanceOf(CurriculumNotFoundException.class);
    }

    // ─── create ─────────────────────────────────────────────────────────────

    @Test
    void create_shouldReturnCurriculumResponse_whenDataIsValid() {
        // Arrange
        CreateCurriculumRequest req = new CreateCurriculumRequest(
                "วิทยาการคอมพิวเตอร์ใหม่", "New Computer Science",
                "Bachelor", 1L, 2L, 120, new BigDecimal("4.0"),
                2567, "ABET", CurriculumStatus.DRAFT, "New curriculum");

        given(facultyRepository.findById(1L)).willReturn(Optional.of(faculty));
        given(departmentRepository.findById(2L)).willReturn(Optional.of(department));
        given(curriculumRepository.countByCurriculumCodeStartingWithAndEffectiveYear(anyString(), anyInt()))
                .willReturn(0);
        given(curriculumRepository.existsByCurriculumCode(anyString())).willReturn(false);
        given(curriculumRepository.save(any(Curriculum.class))).willAnswer(inv -> {
            Curriculum c = inv.getArgument(0);
            setId(c, 1L);
            c.setFaculty(faculty);
            c.setDepartment(department);
            return c;
        });
        willDoNothing().given(auditLogService).log(anyString(), anyString(), any(AuditAction.class),
                any(), any(), any(), any(AuditContext.class));

        // Act
        CurriculumResponse result = curriculumService.create(req, auditCtx);

        // Assert
        assertThat(result.curriculumNameTh()).isEqualTo("วิทยาการคอมพิวเตอร์ใหม่");
        assertThat(result.status()).isEqualTo("DRAFT");
        then(curriculumRepository).should().save(any(Curriculum.class));
    }

    @Test
    void create_shouldDefaultStatusToDraft_whenStatusIsNull() {
        // Arrange
        CreateCurriculumRequest req = new CreateCurriculumRequest(
                "หลักสูตรทดสอบ", "Test Curriculum",
                "Bachelor", 1L, 2L, 120, new BigDecimal("4.0"),
                2567, null, null, null);

        given(facultyRepository.findById(1L)).willReturn(Optional.of(faculty));
        given(departmentRepository.findById(2L)).willReturn(Optional.of(department));
        given(curriculumRepository.countByCurriculumCodeStartingWithAndEffectiveYear(anyString(), anyInt()))
                .willReturn(0);
        given(curriculumRepository.existsByCurriculumCode(anyString())).willReturn(false);
        given(curriculumRepository.save(any(Curriculum.class))).willAnswer(inv -> {
            Curriculum c = inv.getArgument(0);
            setId(c, 1L);
            c.setFaculty(faculty);
            c.setDepartment(department);
            return c;
        });
        willDoNothing().given(auditLogService).log(anyString(), anyString(), any(AuditAction.class),
                any(), any(), any(), any(AuditContext.class));

        // Act
        CurriculumResponse result = curriculumService.create(req, auditCtx);

        // Assert
        assertThat(result.status()).isEqualTo("DRAFT");
    }

    @Test
    void create_shouldThrow_whenFacultyNotFound() {
        // Arrange
        CreateCurriculumRequest req = new CreateCurriculumRequest(
                "ชื่อ", "Name", "Bachelor", 99L, 2L,
                120, new BigDecimal("4.0"), 2567, null, null, null);

        given(facultyRepository.findById(99L)).willReturn(Optional.empty());

        // Act / Assert
        assertThatThrownBy(() -> curriculumService.create(req, auditCtx))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", "FACULTY_NOT_FOUND");
    }

    @Test
    void create_shouldThrow_whenDepartmentNotFound() {
        // Arrange
        CreateCurriculumRequest req = new CreateCurriculumRequest(
                "ชื่อ", "Name", "Bachelor", 1L, 99L,
                120, new BigDecimal("4.0"), 2567, null, null, null);

        given(facultyRepository.findById(1L)).willReturn(Optional.of(faculty));
        given(departmentRepository.findById(99L)).willReturn(Optional.empty());

        // Act / Assert
        assertThatThrownBy(() -> curriculumService.create(req, auditCtx))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", "DEPARTMENT_NOT_FOUND");
    }

    // ─── update ─────────────────────────────────────────────────────────────

    @Test
    void update_shouldUpdateFields_whenCurriculumExists() {
        // Arrange
        UpdateCurriculumRequest req = new UpdateCurriculumRequest(
                "วิทยาการคอมพิวเตอร์ (ปรับปรุง)", null, null,
                null, null, null, null, null, null, CurriculumStatus.ACTIVE, "Updated description");

        given(curriculumRepository.findById(1L)).willReturn(Optional.of(existingCurriculum));
        given(curriculumRepository.save(any(Curriculum.class))).willAnswer(inv -> inv.getArgument(0));
        willDoNothing().given(auditLogService).log(anyString(), anyString(), any(AuditAction.class),
                any(), any(), any(), any(AuditContext.class));

        // Act
        CurriculumResponse result = curriculumService.update(1L, req, auditCtx);

        // Assert
        assertThat(result.curriculumNameTh()).isEqualTo("วิทยาการคอมพิวเตอร์ (ปรับปรุง)");
        assertThat(result.status()).isEqualTo("ACTIVE");
    }

    @Test
    void update_shouldThrowNotFound_whenCurriculumMissing() {
        // Arrange
        UpdateCurriculumRequest req = new UpdateCurriculumRequest(
                null, null, null, null, null, null, null, null, null, null, null);
        given(curriculumRepository.findById(999L)).willReturn(Optional.empty());

        // Act / Assert
        assertThatThrownBy(() -> curriculumService.update(999L, req, auditCtx))
                .isInstanceOf(CurriculumNotFoundException.class);
    }

    // ─── softDelete ──────────────────────────────────────────────────────────

    @Test
    void softDelete_shouldSetStatusToInactive_whenCurriculumExists() {
        // Arrange
        given(curriculumRepository.findById(1L)).willReturn(Optional.of(existingCurriculum));
        given(curriculumRepository.save(any(Curriculum.class))).willAnswer(inv -> inv.getArgument(0));
        willDoNothing().given(auditLogService).log(anyString(), anyString(), any(AuditAction.class),
                any(), any(), any(), any(AuditContext.class));

        // Act
        curriculumService.softDelete(1L, auditCtx);

        // Assert
        assertThat(existingCurriculum.getStatus()).isEqualTo(CurriculumStatus.INACTIVE);
        assertThat(existingCurriculum.getDeletedAt()).isNotNull();
    }

    @Test
    void softDelete_shouldThrowNotFound_whenCurriculumMissing() {
        // Arrange
        given(curriculumRepository.findById(999L)).willReturn(Optional.empty());

        // Act / Assert
        assertThatThrownBy(() -> curriculumService.softDelete(999L, auditCtx))
                .isInstanceOf(CurriculumNotFoundException.class);
    }

    // ─── update — faculty/department change ──────────────────────────────────

    @Test
    void update_shouldChangeFaculty_whenFacultyIdProvided() {
        Faculty newFaculty = new Faculty();
        newFaculty.setCode("SCI");
        newFaculty.setNameTh("คณะวิทยาศาสตร์");
        newFaculty.setNameEn("Faculty of Science");

        // field order: curriculumNameTh, curriculumNameEn, degreeLevel, facultyId, departmentId,
        //              totalCredits, durationYears, effectiveYear, accreditationBody, status, description
        UpdateCurriculumRequest req = new UpdateCurriculumRequest(
                null, null, null, 2L, null, null, null, null, null, null, null);

        given(curriculumRepository.findById(1L)).willReturn(Optional.of(existingCurriculum));
        given(facultyRepository.findById(2L)).willReturn(Optional.of(newFaculty));
        given(curriculumRepository.save(any(Curriculum.class))).willAnswer(inv -> {
            existingCurriculum.setFaculty(newFaculty);
            return existingCurriculum;
        });
        willDoNothing().given(auditLogService).log(anyString(), anyString(), any(AuditAction.class),
                any(), any(), any(), any(AuditContext.class));

        CurriculumResponse result = curriculumService.update(1L, req, auditCtx);

        assertThat(result.facultyId()).isEqualTo(newFaculty.getId());
    }

    @Test
    void update_shouldThrow_whenNewFacultyNotFound() {
        // field order: curriculumNameTh, curriculumNameEn, degreeLevel, facultyId, ...
        UpdateCurriculumRequest req = new UpdateCurriculumRequest(
                null, null, null, 99L, null, null, null, null, null, null, null);

        given(curriculumRepository.findById(1L)).willReturn(Optional.of(existingCurriculum));
        given(facultyRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> curriculumService.update(1L, req, auditCtx))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", "FACULTY_NOT_FOUND");
    }

    @Test
    void update_shouldThrow_whenNewDepartmentNotFound() {
        // field order: curriculumNameTh, curriculumNameEn, degreeLevel, facultyId, departmentId, ...
        UpdateCurriculumRequest req = new UpdateCurriculumRequest(
                null, null, null, null, 99L, null, null, null, null, null, null);

        given(curriculumRepository.findById(1L)).willReturn(Optional.of(existingCurriculum));
        given(departmentRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> curriculumService.update(1L, req, auditCtx))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", "DEPARTMENT_NOT_FOUND");
    }

    // ─── search ──────────────────────────────────────────────────────────────

    @Test
    void search_shouldReturnPagedResults_whenValidParams() {
        var page = new PageImpl<>(List.of(existingCurriculum));
        given(curriculumRepository.search(any(), any(), any(), any(), any())).willReturn(page);

        PagedData<CurriculumResponse> result = curriculumService.search("Bachelor", "ACTIVE", 1L, "CS", 1, 10);

        assertThat(result.items()).hasSize(1);
        assertThat(result.items().get(0).curriculumCode()).isEqualTo("CS-2566-01");
    }

    @Test
    void search_shouldReturnEmptyPage_whenNoResults() {
        var emptyPage = new PageImpl<Curriculum>(List.of());
        given(curriculumRepository.search(any(), any(), any(), any(), any())).willReturn(emptyPage);

        PagedData<CurriculumResponse> result = curriculumService.search(null, null, null, null, 1, 10);

        assertThat(result.items()).isEmpty();
    }

    @Test
    void search_shouldThrowBadRequest_whenStatusIsInvalid() {
        assertThatThrownBy(() -> curriculumService.search(null, "BADSTATUS", null, null, 1, 10))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", "INVALID_STATUS");
    }
}
