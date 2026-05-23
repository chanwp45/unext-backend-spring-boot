package com.unext.backend.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unext.backend.shared.exception.AppException;
import com.unext.backend.shared.response.PaginationMeta;
import com.unext.backend.shared.security.JwtTokenProvider;
import com.unext.backend.user.dto.CreateUserRequest;
import com.unext.backend.user.dto.UpdateUserRequest;
import com.unext.backend.user.dto.UserResponse;
import com.unext.backend.user.entity.UserRole;
import com.unext.backend.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(UserControllerTest.MethodSecurityConfig.class)
class UserControllerTest {

    @TestConfiguration
    @EnableMethodSecurity
    static class MethodSecurityConfig {}

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private UserService userService;
    @MockBean private JwtTokenProvider jwtTokenProvider;

    private UserResponse sampleUser(UUID id) {
        return new UserResponse(id, "admin@example.com", UserRole.ADMIN, true, Instant.now(), Instant.now());
    }

    // ─── POST /v1/users ──────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_shouldReturn201_whenRequestIsValid() throws Exception {
        // Arrange
        UUID newId = UUID.randomUUID();
        CreateUserRequest req = new CreateUserRequest("new@example.com", "password99", UserRole.USER);
        given(userService.create(any(CreateUserRequest.class))).willReturn(sampleUser(newId));

        // Act / Assert
        mockMvc.perform(post("/v1/users").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.email").value("admin@example.com"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_shouldReturn409_whenEmailAlreadyExists() throws Exception {
        // Arrange
        CreateUserRequest req = new CreateUserRequest("dup@example.com", "password99", UserRole.USER);
        given(userService.create(any()))
                .willThrow(new AppException("EMAIL_CONFLICT", "Email already registered.", HttpStatus.CONFLICT));

        // Act / Assert
        mockMvc.perform(post("/v1/users").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_shouldReturn422_whenBodyIsInvalid() throws Exception {
        // Arrange — password too short, email not valid
        String badBody = """
                {"email": "bad-email", "password": "short", "role": "USER"}
                """;

        // Act / Assert
        mockMvc.perform(post("/v1/users").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badBody))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @WithMockUser(roles = "USER")
    void create_shouldReturn403_whenNotAdmin() throws Exception {
        // Arrange
        CreateUserRequest req = new CreateUserRequest("new@example.com", "password99", UserRole.USER);

        // Act / Assert
        mockMvc.perform(post("/v1/users").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    // ─── GET /v1/users ───────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void findAll_shouldReturn200WithPagedUsers() throws Exception {
        // Arrange
        UUID id = UUID.randomUUID();
        var result = new UserService.PageResult<>(
                List.of(sampleUser(id)), PaginationMeta.of(1, 20, 1));
        given(userService.findAll(anyInt(), anyInt())).willReturn(result);

        // Act / Assert
        mockMvc.perform(get("/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.items[0].email").value("admin@example.com"));
    }

    // ─── GET /v1/users/{id} ──────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void findById_shouldReturn200_whenUserExists() throws Exception {
        // Arrange
        UUID id = UUID.randomUUID();
        given(userService.findById(id)).willReturn(sampleUser(id));

        // Act / Assert
        mockMvc.perform(get("/v1/users/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("admin@example.com"));
    }

    // ─── PATCH /v1/users/{id} ────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void update_shouldReturn200_whenUserExists() throws Exception {
        // Arrange
        UUID id = UUID.randomUUID();
        UpdateUserRequest req = new UpdateUserRequest(null, UserRole.STAFF, true);
        given(userService.update(eq(id), any(UpdateUserRequest.class))).willReturn(sampleUser(id));

        // Act / Assert
        mockMvc.perform(patch("/v1/users/{id}", id).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    // ─── DELETE /v1/users/{id} ───────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    void delete_shouldReturn204_whenUserExists() throws Exception {
        // Arrange
        UUID id = UUID.randomUUID();
        willDoNothing().given(userService).delete(id);

        // Act / Assert
        mockMvc.perform(delete("/v1/users/{id}", id).with(csrf()))
                .andExpect(status().isNoContent());
    }
}
