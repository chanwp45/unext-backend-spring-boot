package com.unext.backend.faculty.controller;

import com.unext.backend.faculty.dto.DepartmentResponse;
import com.unext.backend.faculty.dto.FacultyResponse;
import com.unext.backend.faculty.service.FacultyService;
import com.unext.backend.shared.exception.AppException;
import com.unext.backend.shared.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FacultyController.class)
class FacultyControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private FacultyService facultyService;
    @MockBean private JwtTokenProvider jwtTokenProvider;

    // ─── GET /v1/faculties ───────────────────────────────────────────────────

    @Test
    @WithMockUser
    void findAll_shouldReturn200WithFacultyList() throws Exception {
        // Arrange
        given(facultyService.findAll()).willReturn(List.of(
                new FacultyResponse(1L, "ENG", "คณะวิศวกรรมศาสตร์", "Faculty of Engineering"),
                new FacultyResponse(2L, "SCI", "คณะวิทยาศาสตร์", "Faculty of Science")
        ));

        // Act / Assert
        mockMvc.perform(get("/v1/faculties"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].code").value("ENG"))
                .andExpect(jsonPath("$.data[1].code").value("SCI"));
    }

    @Test
    @WithMockUser
    void findAll_shouldReturn200WithEmptyList_whenNoFaculties() throws Exception {
        // Arrange
        given(facultyService.findAll()).willReturn(List.of());

        // Act / Assert
        mockMvc.perform(get("/v1/faculties"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    // ─── GET /v1/faculties/{id}/departments ──────────────────────────────────

    @Test
    @WithMockUser
    void findDepartments_shouldReturn200WithDepartmentList() throws Exception {
        // Arrange
        given(facultyService.findDepartmentsByFaculty(1L)).willReturn(List.of(
                new DepartmentResponse(10L, 1L, "CS", "สาขาวิทยาการคอมพิวเตอร์", "Computer Science")
        ));

        // Act / Assert
        mockMvc.perform(get("/v1/faculties/{id}/departments", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].code").value("CS"))
                .andExpect(jsonPath("$.data[0].name_th").value("สาขาวิทยาการคอมพิวเตอร์"));
    }

    @Test
    @WithMockUser
    void findDepartments_shouldReturn404_whenFacultyNotFound() throws Exception {
        // Arrange
        given(facultyService.findDepartmentsByFaculty(999L))
                .willThrow(new AppException("FACULTY_NOT_FOUND",
                        "Faculty with id '999' was not found.", HttpStatus.NOT_FOUND));

        // Act / Assert
        mockMvc.perform(get("/v1/faculties/{id}/departments", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("error"));
    }

    @Test
    void findAll_shouldReturn401_whenNotAuthenticated() throws Exception {
        // Act / Assert
        mockMvc.perform(get("/v1/faculties"))
                .andExpect(status().isUnauthorized());
    }
}
