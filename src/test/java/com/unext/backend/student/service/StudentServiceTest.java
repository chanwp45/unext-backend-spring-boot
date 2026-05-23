package com.unext.backend.student.service;

import com.unext.backend.audit.entity.AuditAction;
import com.unext.backend.audit.service.AuditContext;
import com.unext.backend.audit.service.AuditLogService;
import com.unext.backend.curriculum.entity.Curriculum;
import com.unext.backend.curriculum.entity.CurriculumStatus;
import com.unext.backend.curriculum.repository.CurriculumRepository;
import com.unext.backend.shared.exception.AppException;
import com.unext.backend.shared.response.PagedData;
import com.unext.backend.student.dto.CreateStudentRequest;
import com.unext.backend.student.dto.StudentResponse;
import com.unext.backend.student.dto.UpdateStudentRequest;
import com.unext.backend.student.dto.UpdateStudentStatusRequest;
import com.unext.backend.student.entity.Gender;
import com.unext.backend.student.entity.Student;
import com.unext.backend.student.entity.StudentStatus;
import com.unext.backend.student.exception.StudentNotFoundException;
import com.unext.backend.student.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock private StudentRepository studentRepository;
    @Mock private CurriculumRepository curriculumRepository;
    @Mock private AuditLogService auditLogService;

    @InjectMocks private StudentService studentService;

    private AuditContext auditCtx;
    private Curriculum curriculum;
    private Student existingStudent;

    @BeforeEach
    void setUp() {
        auditCtx = new AuditContext("admin", "Admin User", "STAFF", "127.0.0.1", "TestAgent", null, null);

        curriculum = new Curriculum();
        curriculum.setCurriculumCode("CS-2566-01");
        curriculum.setCurriculumNameTh("วิทยาการคอมพิวเตอร์");
        curriculum.setCurriculumNameEn("Computer Science");
        curriculum.setDegreeLevel("Bachelor");
        curriculum.setTotalCredits(120);
        curriculum.setDurationYears(new BigDecimal("4.0"));
        curriculum.setEffectiveYear(2566);
        curriculum.setStatus(CurriculumStatus.ACTIVE);
        curriculum.setCreatedBy("admin");

        existingStudent = new Student();
        existingStudent.setStudentId("6701000001");
        existingStudent.setNationalId("1234567890123");
        existingStudent.setEmail("student@example.com");
        existingStudent.setPhone("0812345678");
        existingStudent.setAddress("123 Test Street");
        existingStudent.setTitleTh("นาย");
        existingStudent.setFirstNameTh("สมชาย");
        existingStudent.setLastNameTh("ทดสอบ");
        existingStudent.setFirstNameEn("Somchai");
        existingStudent.setLastNameEn("Test");
        existingStudent.setDateOfBirth(LocalDate.of(2000, 1, 15));
        existingStudent.setGender(Gender.MALE);
        existingStudent.setNationality("Thai");
        existingStudent.setCurriculum(curriculum);
        existingStudent.setAdmissionYear(2567);
        existingStudent.setStudentStatus(StudentStatus.STUDYING);
        existingStudent.setCreatedBy("admin");
    }

    // ─── findById ───────────────────────────────────────────────────────────

    @Test
    void findById_shouldReturnStudent_whenExists() {
        // Arrange
        given(studentRepository.findById("6701000001")).willReturn(Optional.of(existingStudent));

        // Act
        StudentResponse result = studentService.findById("6701000001");

        // Assert
        assertThat(result.studentId()).isEqualTo("6701000001");
        assertThat(result.email()).isEqualTo("student@example.com");
    }

    @Test
    void findById_shouldThrowNotFound_whenStudentMissing() {
        // Arrange
        given(studentRepository.findById(anyString())).willReturn(Optional.empty());

        // Act / Assert
        assertThatThrownBy(() -> studentService.findById("NONEXISTENT"))
                .isInstanceOf(StudentNotFoundException.class);
    }

    // ─── register ────────────────────────────────────────────────────────────

    @Test
    void register_shouldCreateStudent_whenDataIsValid() {
        // Arrange
        CreateStudentRequest req = new CreateStudentRequest(
                "9876543210987", "นาย", "สมหมาย", "ใหม่",
                "Sommai", "New", LocalDate.of(2001, 5, 20),
                Gender.MALE, "Thai", "new@example.com",
                "0899999999", "456 New St", 1L, 2567, null, null);

        given(studentRepository.existsByNationalId("9876543210987")).willReturn(false);
        given(studentRepository.existsByEmail("new@example.com")).willReturn(false);
        given(curriculumRepository.findById(1L)).willReturn(Optional.of(curriculum));
        given(studentRepository.countByAdmissionYearAndCurriculumId(2567, 1L)).willReturn(0);
        given(studentRepository.save(any(Student.class))).willAnswer(inv -> {
            Student s = inv.getArgument(0);
            s.setCurriculum(curriculum);
            return s;
        });
        willDoNothing().given(auditLogService).log(anyString(), anyString(), any(AuditAction.class),
                any(), any(), any(), any(AuditContext.class));

        // Act
        StudentResponse result = studentService.register(req, auditCtx);

        // Assert
        assertThat(result.email()).isEqualTo("new@example.com");
        assertThat(result.studentStatus()).isEqualTo("STUDYING");
        then(studentRepository).should().save(any(Student.class));
    }

    @Test
    void register_shouldThrowConflict_whenNationalIdAlreadyExists() {
        // Arrange
        CreateStudentRequest req = new CreateStudentRequest(
                "1234567890123", "นาย", "ซ้ำ", "รหัส",
                "Dup", "Id", LocalDate.of(2001, 1, 1),
                Gender.MALE, "Thai", "dup@example.com",
                "0811111111", "789 Dup St", 1L, 2567, null, null);

        given(studentRepository.existsByNationalId("1234567890123")).willReturn(true);

        // Act / Assert
        assertThatThrownBy(() -> studentService.register(req, auditCtx))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", "NATIONAL_ID_CONFLICT");
    }

    @Test
    void register_shouldThrowConflict_whenEmailAlreadyExists() {
        // Arrange
        CreateStudentRequest req = new CreateStudentRequest(
                "1111111111111", "นาย", "ซ้ำ", "อีเมล",
                "Dup", "Email", LocalDate.of(2001, 1, 1),
                Gender.MALE, "Thai", "student@example.com",
                "0811111111", "789 Dup St", 1L, 2567, null, null);

        given(studentRepository.existsByNationalId("1111111111111")).willReturn(false);
        given(studentRepository.existsByEmail("student@example.com")).willReturn(true);

        // Act / Assert
        assertThatThrownBy(() -> studentService.register(req, auditCtx))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", "EMAIL_CONFLICT");
    }

    @Test
    void register_shouldThrow_whenCurriculumNotFound() {
        // Arrange
        CreateStudentRequest req = new CreateStudentRequest(
                "2222222222222", "นาย", "ไม่มี", "หลักสูตร",
                "No", "Curriculum", LocalDate.of(2001, 1, 1),
                Gender.MALE, "Thai", "nocurr@example.com",
                "0822222222", "000 Nowhere", 999L, 2567, null, null);

        given(studentRepository.existsByNationalId("2222222222222")).willReturn(false);
        given(studentRepository.existsByEmail("nocurr@example.com")).willReturn(false);
        given(curriculumRepository.findById(999L)).willReturn(Optional.empty());

        // Act / Assert
        assertThatThrownBy(() -> studentService.register(req, auditCtx))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", "CURRICULUM_NOT_FOUND");
    }

    // ─── update ──────────────────────────────────────────────────────────────

    @Test
    void update_shouldUpdateContactFields_forStudentRole() {
        // Arrange
        UpdateStudentRequest req = new UpdateStudentRequest(
                null, null, null, null, null, null, null, null,
                "updated@example.com", "0899990000", "New Address", null, null, null);

        given(studentRepository.findById("6701000001")).willReturn(Optional.of(existingStudent));
        given(studentRepository.existsByEmail("updated@example.com")).willReturn(false);
        given(studentRepository.save(any(Student.class))).willAnswer(inv -> inv.getArgument(0));
        willDoNothing().given(auditLogService).log(anyString(), anyString(), any(AuditAction.class),
                any(), any(), any(), any(AuditContext.class));

        // Act
        StudentResponse result = studentService.update("6701000001", req, false, auditCtx);

        // Assert
        assertThat(result.email()).isEqualTo("updated@example.com");
        assertThat(result.phone()).isEqualTo("0899990000");
    }

    @Test
    void update_shouldUpdateStaffOnlyFields_whenIsStaffTrue() {
        // Arrange
        UpdateStudentRequest req = new UpdateStudentRequest(
                "นางสาว", "สมศรี", "อัปเดต", "Somsri", "Updated",
                LocalDate.of(2001, 6, 15), Gender.FEMALE, "Thai",
                null, null, null, null, null, null);

        given(studentRepository.findById("6701000001")).willReturn(Optional.of(existingStudent));
        given(studentRepository.save(any(Student.class))).willAnswer(inv -> inv.getArgument(0));
        willDoNothing().given(auditLogService).log(anyString(), anyString(), any(AuditAction.class),
                any(), any(), any(), any(AuditContext.class));

        // Act
        StudentResponse result = studentService.update("6701000001", req, true, auditCtx);

        // Assert
        assertThat(result.firstNameTh()).isEqualTo("สมศรี");
        assertThat(result.gender()).isEqualTo("FEMALE");
    }

    @Test
    void update_shouldThrowNotFound_whenStudentMissing() {
        // Arrange
        UpdateStudentRequest req = new UpdateStudentRequest(
                null, null, null, null, null, null, null, null, null, null, null, null, null, null);
        given(studentRepository.findById("NOPE")).willReturn(Optional.empty());

        // Act / Assert
        assertThatThrownBy(() -> studentService.update("NOPE", req, true, auditCtx))
                .isInstanceOf(StudentNotFoundException.class);
    }

    // ─── updateStatus ─────────────────────────────────────────────────────────

    @Test
    void updateStatus_shouldChangeStudentStatus() {
        // Arrange
        UpdateStudentStatusRequest req = new UpdateStudentStatusRequest(
                StudentStatus.GRADUATED, null, "Completed all courses", null);

        given(studentRepository.findById("6701000001")).willReturn(Optional.of(existingStudent));
        given(studentRepository.save(any(Student.class))).willAnswer(inv -> inv.getArgument(0));
        willDoNothing().given(auditLogService).log(anyString(), anyString(), any(AuditAction.class),
                any(), any(), any(), any(AuditContext.class));

        // Act
        StudentResponse result = studentService.updateStatus("6701000001", req, auditCtx);

        // Assert
        assertThat(result.studentStatus()).isEqualTo("GRADUATED");
    }

    @Test
    void updateStatus_shouldThrowNotFound_whenStudentMissing() {
        // Arrange
        UpdateStudentStatusRequest req = new UpdateStudentStatusRequest(
                StudentStatus.GRADUATED, null, null, null);
        given(studentRepository.findById("NOPE")).willReturn(Optional.empty());

        // Act / Assert
        assertThatThrownBy(() -> studentService.updateStatus("NOPE", req, auditCtx))
                .isInstanceOf(StudentNotFoundException.class);
    }

    // ─── updatePhoto ─────────────────────────────────────────────────────────

    @Test
    void updatePhoto_shouldUpdatePhotoUrl_whenStudentExists() {
        given(studentRepository.findById("6701000001")).willReturn(Optional.of(existingStudent));
        given(studentRepository.save(any(Student.class))).willAnswer(inv -> inv.getArgument(0));
        willDoNothing().given(auditLogService).log(anyString(), anyString(), any(AuditAction.class),
                any(), any(), any(), any(AuditContext.class));

        StudentResponse result = studentService.updatePhoto("6701000001", "https://cdn.example.com/photo.jpg", auditCtx);

        assertThat(result.photoUrl()).isEqualTo("https://cdn.example.com/photo.jpg");
    }

    @Test
    void updatePhoto_shouldThrowNotFound_whenStudentMissing() {
        given(studentRepository.findById("NOPE")).willReturn(Optional.empty());

        assertThatThrownBy(() -> studentService.updatePhoto("NOPE", "https://photo.jpg", auditCtx))
                .isInstanceOf(StudentNotFoundException.class);
    }

    // ─── update — staff-only curriculum change ────────────────────────────────

    @Test
    void update_shouldChangeCurriculum_whenStaffProvidesCurriculumId() {
        Curriculum newCurriculum = new Curriculum();
        newCurriculum.setCurriculumCode("IT-2566-01");
        newCurriculum.setCurriculumNameTh("เทคโนโลยีสารสนเทศ");
        newCurriculum.setCurriculumNameEn("Information Technology");
        newCurriculum.setDegreeLevel("Bachelor");
        newCurriculum.setTotalCredits(126);
        newCurriculum.setDurationYears(new BigDecimal("4.0"));
        newCurriculum.setEffectiveYear(2566);
        newCurriculum.setStatus(CurriculumStatus.ACTIVE);
        newCurriculum.setCreatedBy("admin");

        // field order: titleTh, firstNameTh, lastNameTh, firstNameEn, lastNameEn,
        //              dateOfBirth, gender, nationality, email, phone, address, curriculumId, guardianName, guardianPhone
        UpdateStudentRequest req = new UpdateStudentRequest(
                null, null, null, null, null, null, null, null,
                null, null, null, 2L, null, null);

        given(studentRepository.findById("6701000001")).willReturn(Optional.of(existingStudent));
        given(curriculumRepository.findById(2L)).willReturn(Optional.of(newCurriculum));
        given(studentRepository.save(any(Student.class))).willAnswer(inv -> {
            existingStudent.setCurriculum(newCurriculum);
            return existingStudent;
        });
        willDoNothing().given(auditLogService).log(anyString(), anyString(), any(AuditAction.class),
                any(), any(), any(), any(AuditContext.class));

        StudentResponse result = studentService.update("6701000001", req, true, auditCtx);

        assertThat(result.curriculum().curriculumId()).isEqualTo(newCurriculum.getId());
    }

    @Test
    void update_shouldThrowConflict_whenEmailAlreadyTaken() {
        UpdateStudentRequest req = new UpdateStudentRequest(
                null, null, null, null, null, null, null, null,
                "taken@example.com", null, null, null, null, null);

        given(studentRepository.findById("6701000001")).willReturn(Optional.of(existingStudent));
        given(studentRepository.existsByEmail("taken@example.com")).willReturn(true);

        assertThatThrownBy(() -> studentService.update("6701000001", req, false, auditCtx))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", "EMAIL_CONFLICT");
    }

    // ─── search ───────────────────────────────────────────────────────────────

    @Test
    void search_shouldReturnPagedResults_whenValidParams() {
        var page = new PageImpl<>(List.of(existingStudent));
        given(studentRepository.search(any(), any(), any(), any(), any())).willReturn(page);

        PagedData<StudentResponse> result = studentService.search(null, null, null, null, 1, 10);

        assertThat(result.items()).hasSize(1);
        assertThat(result.items().get(0).studentId()).isEqualTo("6701000001");
    }

    @Test
    void search_shouldReturnPagedResults_whenFilterByValidStatus() {
        var page = new PageImpl<>(List.of(existingStudent));
        given(studentRepository.search(any(), any(), any(), any(), any())).willReturn(page);

        PagedData<StudentResponse> result = studentService.search("สมชาย", "STUDYING", null, 2567, 1, 10);

        assertThat(result.items()).hasSize(1);
    }

    @Test
    void search_shouldThrowBadRequest_whenStatusIsInvalid() {
        assertThatThrownBy(() -> studentService.search(null, "INVALID_STATUS", null, null, 1, 10))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", "INVALID_STATUS");
    }
}
