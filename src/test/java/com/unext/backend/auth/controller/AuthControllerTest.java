package com.unext.backend.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unext.backend.auth.dto.LoginRequest;
import com.unext.backend.auth.dto.LoginResponse;
import com.unext.backend.auth.dto.RefreshTokenRequest;
import com.unext.backend.auth.service.AuthService;
import com.unext.backend.shared.exception.AppException;
import com.unext.backend.shared.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private AuthService authService;
    @MockBean private JwtTokenProvider jwtTokenProvider;

    private LoginResponse sampleLoginResponse() {
        return new LoginResponse("access.token", "refresh-uuid", "Bearer", 900L);
    }

    @Test
    @WithMockUser
    void login_shouldReturn200_whenCredentialsAreValid() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest("user@example.com", "password123");
        given(authService.login(any(LoginRequest.class))).willReturn(sampleLoginResponse());

        // Act / Assert
        mockMvc.perform(post("/v1/auth/login").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.access_token").value("access.token"))
                .andExpect(jsonPath("$.data.token_type").value("Bearer"));
    }

    @Test
    @WithMockUser
    void login_shouldReturn401_whenCredentialsAreInvalid() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest("user@example.com", "wrongpass");
        given(authService.login(any(LoginRequest.class)))
                .willThrow(new AppException("INVALID_CREDENTIALS", "Invalid email or password.", HttpStatus.UNAUTHORIZED));

        // Act / Assert
        mockMvc.perform(post("/v1/auth/login").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value("error"));
    }

    @Test
    @WithMockUser
    void login_shouldReturn422_whenRequestBodyIsInvalid() throws Exception {
        // Arrange — empty email and password triggers validation
        String invalidBody = """
                {"email": "", "password": ""}
                """;

        // Act / Assert
        mockMvc.perform(post("/v1/auth/login").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidBody))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @WithMockUser
    void refresh_shouldReturn200_whenTokenIsValid() throws Exception {
        // Arrange
        RefreshTokenRequest request = new RefreshTokenRequest("valid-refresh-token");
        given(authService.refresh(anyString())).willReturn(sampleLoginResponse());

        // Act / Assert
        mockMvc.perform(post("/v1/auth/refresh-token").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.access_token").value("access.token"));
    }

    @Test
    @WithMockUser
    void refresh_shouldReturn401_whenTokenIsInvalid() throws Exception {
        // Arrange
        RefreshTokenRequest request = new RefreshTokenRequest("invalid-token");
        given(authService.refresh(anyString()))
                .willThrow(new AppException("INVALID_TOKEN", "Refresh token is invalid.", HttpStatus.UNAUTHORIZED));

        // Act / Assert
        mockMvc.perform(post("/v1/auth/refresh-token").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "user-uuid-123")
    void logout_shouldReturn204_whenAuthenticated() throws Exception {
        // Arrange
        willDoNothing().given(authService).logout(anyString());

        // Act / Assert
        mockMvc.perform(post("/v1/auth/logout").with(csrf()))
                .andExpect(status().isNoContent());
    }
}
