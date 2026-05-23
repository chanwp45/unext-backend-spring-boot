package com.unext.backend.curriculum.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unext.backend.audit.service.AuditLogService;
import com.unext.backend.curriculum.dto.CreateCurriculumRequest;
import com.unext.backend.curriculum.dto.CurriculumResponse;
import com.unext.backend.curriculum.dto.UpdateCurriculumRequest;
import com.unext.backend.curriculum.entity.CurriculumStatus;
import com.unext.backend.curriculum.exception.CurriculumNotFoundException;
import com.unext.backend.curriculum.service.CurriculumService;
import com.unext.backend.shared.exception.AppException;
import com.unext.backend.shared.response.PagedData;
import com.unext.backend.shared.response.PaginationMeta;
import com.unext.backend.shared.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CurriculumController.class)
class CurriculumControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private CurriculumService curriculumService;
    @MockBean private AuditLogService auditLogService;
    @MockBean private JwtTokenProvider jwtTokenProvider;

    private CurriculumResponse sampleCurriculum(Long id) {
        return new CurriculumResponse(
                id, "CS-2566-01",
                "วิทยาการคอมพิวเตอร์", "Computer Science",
                "Bachelor", 1L, "คณะวิศวกรรมศาสตร์",
                2L, "สาขาวิทยาการคอมพิวเตอร์",
                120, new BigDecimal("4.0"), 2566, "ABET",
                "ACTIVE", "Sample curriculum",
                "admin", null, Instant.now(), Instant.now());
    }

    // ─── GET /v1/curricula ───────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "STAFF")
    void search_shouldReturn200WithPagedResults() throws Exception {
        // Arrange
        PagedData<CurriculumResponse> paged = new PagedData<>(
                List.of(sampleCurriculum(1L)),
                PaginationMeta.of(1, 20, 1));
        given(curriculumService.search(any(), any(), any(), any(), anyInt(), anyInt()))
                .willReturn(paged);

        // Act / Assert
        mockMvc.perform(get("/v1/curricula"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].curriculum_code").value("CS-2566-01"));
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void search_shouldReturn400_whenStatusIsInvalid() throws Exception {
        // Arrange
        given(curriculumService.search(any(), any(), any(), any(), anyInt(), anyInt()))
                .willThrow(new AppException("INVALID_STATUS", "Invalid status value.", HttpStatus.BAD_REQUEST));

        // Act / Assert
        mockMvc.perform(get("/v1/curricula").param("status", "BADVALUE"))
                .andExpect(status().isBadRequest());
    }

    // ─── GET /v1/curricula/{id} ──────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "STAFF")
    void findById_shouldReturn200_whenCurriculumExists() throws Exception {
        // Arrange
        given(curriculumService.findById(1L)).willReturn(sampleCurriculum(1L));

        // Act / Assert
        mockMvc.perform(get("/v1/curricula/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.curriculum_code").value("CS-2566-01"));
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void findById_shouldReturn404_whenCurriculumMissing() throws Exception {
        // Arrange
        given(curriculumService.findById(999L))
                .willThrow(new CurriculumNotFoundException(999L));

        // Act / Assert
        mockMvc.perform(get("/v1/curricula/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    // ─── POST /v1/curricula ──────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "STAFF")
    void create_shouldReturn201_whenRequestIsValid() throws Exception {
        // Arrange
        CreateCurriculumRequest req = new CreateCurriculumRequest(
                "หลักสูตรใหม่", "New Curriculum", "Bachelor",
                1L, 2L, 120, new BigDecimal("4.0"),
                2567, null, CurriculumStatus.DRAFT, null);
        given(curriculumService.create(any(), any())).willReturn(sampleCurriculum(1L));

        // Act / Assert
        mockMvc.perform(post("/v1/curricula").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.curriculum_code").value("CS-2566-01"));
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void create_shouldReturn422_whenBodyIsInvalid() throws Exception {
        // Arrange — missing required fields
        String badBody = """
                {"curriculumNameTh": "", "degreeLevel": "Bachelor"}
                """;

        // Act / Assert
        mockMvc.perform(post("/v1/curricula").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badBody))
                .andExpect(status().isUnprocessableEntity());
    }

    // ─── PUT /v1/curricula/{id} ──────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "STAFF")
    void update_shouldReturn200_whenCurriculumExists() throws Exception {
        // Arrange
        UpdateCurriculumRequest req = new UpdateCurriculumRequest(
                "ชื่อใหม่", null, null, null, null, null, null, null, null, CurriculumStatus.ACTIVE, null);
        given(curriculumService.update(eq(1L), any(), any())).willReturn(sampleCurriculum(1L));

        // Act / Assert
        mockMvc.perform(put("/v1/curricula/{id}", 1L).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    // ─── DELETE /v1/curricula/{id} ───────────────────────────────────────────

    @Test
    @WithMockUser(roles = "STAFF")
    void softDelete_shouldReturn200_whenCurriculumExists() throws Exception {
        // Arrange
        willDoNothing().given(curriculumService).softDelete(anyLong(), any());

        // Act / Assert
        mockMvc.perform(delete("/v1/curricula/{id}", 1L).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Curriculum deactivated successfully"));
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void softDelete_shouldReturn404_whenCurriculumMissing() throws Exception {
        // Arrange
        willThrow(new CurriculumNotFoundException(999L))
                .given(curriculumService).softDelete(eq(999L), any());

        // Act / Assert
        mockMvc.perform(delete("/v1/curricula/{id}", 999L).with(csrf()))
                .andExpect(status().isNotFound());
    }
}
