package com.unext.backend.student.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unext.backend.audit.service.AuditLogService;
import com.unext.backend.shared.exception.AppException;
import com.unext.backend.shared.response.PagedData;
import com.unext.backend.shared.response.PaginationMeta;
import com.unext.backend.shared.security.JwtTokenProvider;
import com.unext.backend.student.dto.CreateStudentRequest;
import com.unext.backend.student.dto.StudentResponse;
import com.unext.backend.student.dto.UpdateStudentStatusRequest;
import com.unext.backend.student.entity.Gender;
import com.unext.backend.student.entity.StudentStatus;
import com.unext.backend.student.exception.StudentNotFoundException;
import com.unext.backend.student.service.StudentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StudentController.class)
class StudentControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private StudentService studentService;
    @MockBean private AuditLogService auditLogService;
    @MockBean private JwtTokenProvider jwtTokenProvider;

    private StudentResponse sampleStudent(String studentId) {
        return new StudentResponse(
                studentId, "1234567890123", "นาย",
                "สมชาย", "ทดสอบ", "Somchai", "Test",
                LocalDate.of(2000, 1, 15), "MALE", "Thai",
                "student@example.com", "0812345678", "123 Test St",
                new StudentResponse.CurriculumInfo(1L, "CS-2566-01", "วิทยาการคอมพิวเตอร์"),
                2567, "STUDYING", null, null, null,
                Instant.now(), Instant.now());
    }

    // ─── GET /v1/students ────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "STAFF")
    void search_shouldReturn200WithPagedStudents() throws Exception {
        // Arrange
        PagedData<StudentResponse> paged = new PagedData<>(
                List.of(sampleStudent("6701000001")),
                PaginationMeta.of(1, 20, 1));
        given(studentService.search(any(), any(), any(), any(), anyInt(), anyInt()))
                .willReturn(paged);

        // Act / Assert
        mockMvc.perform(get("/v1/students"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].student_id").value("6701000001"))
                .andExpect(jsonPath("$.data.items[0].student_status").value("STUDYING"));
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void search_shouldReturn400_whenStatusIsInvalid() throws Exception {
        // Arrange
        given(studentService.search(any(), any(), any(), any(), anyInt(), anyInt()))
                .willThrow(new AppException("INVALID_STATUS", "Invalid status.", HttpStatus.BAD_REQUEST));

        // Act / Assert
        mockMvc.perform(get("/v1/students").param("status", "INVALID"))
                .andExpect(status().isBadRequest());
    }

    // ─── GET /v1/students/{studentId} ────────────────────────────────────────

    @Test
    @WithMockUser(roles = "STAFF")
    void findById_shouldReturn200_whenStudentExists() throws Exception {
        // Arrange
        given(studentService.findById("6701000001")).willReturn(sampleStudent("6701000001"));

        // Act / Assert
        mockMvc.perform(get("/v1/students/{studentId}", "6701000001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("student@example.com"));
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void findById_shouldReturn404_whenStudentMissing() throws Exception {
        // Arrange
        given(studentService.findById("NOPE")).willThrow(new StudentNotFoundException("NOPE"));

        // Act / Assert
        mockMvc.perform(get("/v1/students/{studentId}", "NOPE"))
                .andExpect(status().isNotFound());
    }

    // ─── POST /v1/students ───────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "STAFF")
    void register_shouldReturn201_whenRequestIsValid() throws Exception {
        // Arrange
        CreateStudentRequest req = new CreateStudentRequest(
                "1234567890123", "นาย", "สมชาย", "ทดสอบ",
                "Somchai", "Test", LocalDate.of(2000, 1, 15),
                Gender.MALE, "Thai", "student@example.com",
                "0812345678", "123 Test St", 1L, 2567, null, null);
        given(studentService.register(any(), any())).willReturn(sampleStudent("6701000001"));

        // Act / Assert
        mockMvc.perform(post("/v1/students").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.student_id").value("6701000001"));
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void register_shouldReturn409_whenNationalIdConflict() throws Exception {
        // Arrange
        CreateStudentRequest req = new CreateStudentRequest(
                "1234567890123", "นาย", "สมชาย", "ทดสอบ",
                "Somchai", "Test", LocalDate.of(2000, 1, 15),
                Gender.MALE, "Thai", "student@example.com",
                "0812345678", "123 Test St", 1L, 2567, null, null);
        given(studentService.register(any(), any()))
                .willThrow(new AppException("NATIONAL_ID_CONFLICT", "เลขบัตรประชาชนนี้มีในระบบแล้ว", HttpStatus.CONFLICT));

        // Act / Assert
        mockMvc.perform(post("/v1/students").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict());
    }

    // ─── PATCH /v1/students/{studentId}/status ───────────────────────────────

    @Test
    @WithMockUser(roles = "STAFF")
    void updateStatus_shouldReturn200_whenStudentExists() throws Exception {
        // Arrange
        UpdateStudentStatusRequest req = new UpdateStudentStatusRequest(
                StudentStatus.GRADUATED, null, "สำเร็จการศึกษา", null);
        StudentResponse graduated = sampleStudent("6701000001");
        given(studentService.updateStatus(eq("6701000001"), any(), any())).willReturn(graduated);

        // Act / Assert
        mockMvc.perform(patch("/v1/students/{studentId}/status", "6701000001").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    // ─── PATCH /v1/students/{studentId}/photo ────────────────────────────────

    @Test
    @WithMockUser(roles = "STAFF")
    void updatePhoto_shouldReturn200_whenStudentExists() throws Exception {
        // Arrange
        given(studentService.updatePhoto(eq("6701000001"), anyString(), any()))
                .willReturn(sampleStudent("6701000001"));
        String body = """
                {"photoUrl": "https://cdn.example.com/photo.jpg"}
                """;

        // Act / Assert
        mockMvc.perform(patch("/v1/students/{studentId}/photo", "6701000001").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
    }

    @Test
    void search_shouldReturn401_whenNotAuthenticated() throws Exception {
        // Act / Assert
        mockMvc.perform(get("/v1/students"))
                .andExpect(status().isUnauthorized());
    }
}
