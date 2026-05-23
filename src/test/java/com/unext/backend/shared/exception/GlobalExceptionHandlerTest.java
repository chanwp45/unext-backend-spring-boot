package com.unext.backend.shared.exception;

import com.unext.backend.shared.response.ApiResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;

import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void handleAppException_shouldReturnCorrectStatusAndCode() {
        // Arrange
        AppException ex = new AppException("TEST_CODE", "Something went wrong", HttpStatus.CONFLICT);

        // Act
        ResponseEntity<ApiResponse<Void>> response = handler.handleAppException(ex);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        ApiResponse<Void> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.code()).isEqualTo(409);
        assertThat(body.message()).isEqualTo("Something went wrong");
        assertThat(body.status()).isEqualTo("error");
    }

    @Test
    void handleAppException_shouldReturn401_forUnauthorized() {
        // Arrange
        AppException ex = new AppException("INVALID_CREDENTIALS", "Invalid credentials", HttpStatus.UNAUTHORIZED);

        // Act
        ResponseEntity<ApiResponse<Void>> response = handler.handleAppException(ex);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo(401);
    }

    @Test
    void handleConstraintViolation_shouldReturn422WithViolations() {
        // Arrange
        Path path = mock(Path.class);
        when(path.toString()).thenReturn("email");

        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> violation = mock(ConstraintViolation.class);
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage()).thenReturn("must not be blank");

        ConstraintViolationException ex = new ConstraintViolationException(Set.of(violation));

        // Act
        ResponseEntity<ApiResponse<Void>> response = handler.handleConstraintViolation(ex);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().errors()).isNotEmpty();
        assertThat(response.getBody().errors().get(0).field()).isEqualTo("email");
        assertThat(response.getBody().errors().get(0).message()).isEqualTo("must not be blank");
    }

    @Test
    void handleAuthentication_shouldReturn401() {
        // Arrange
        AuthenticationException ex = new AuthenticationException("Unauthorized") {};

        // Act
        ResponseEntity<ApiResponse<Void>> response = handler.handleAuthentication(ex);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo(401);
    }

    @Test
    void handleAccessDenied_shouldReturn403() {
        // Arrange
        AccessDeniedException ex = new AccessDeniedException("Forbidden");

        // Act
        ResponseEntity<ApiResponse<Void>> response = handler.handleAccessDenied(ex);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo(403);
    }

    @Test
    void handleGeneric_shouldReturn500() {
        // Arrange
        Exception ex = new RuntimeException("Unexpected failure");

        // Act
        ResponseEntity<ApiResponse<Void>> response = handler.handleGeneric(ex);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo(500);
    }
}
