package com.unext.backend.shared.security;

import com.unext.backend.user.entity.UserRole;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class JwtTokenProviderTest {

    private static final String SECRET = "test-secret-key-must-be-at-least-32-bytes!";
    private static final long EXPIRATION_MS = 900_000L;          // 15 min
    private static final long REFRESH_EXPIRATION_MS = 604_800_000L; // 7 days

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(SECRET, EXPIRATION_MS, REFRESH_EXPIRATION_MS);
    }

    @Test
    void generateAccessToken_shouldReturnSignedJwt_withCorrectClaims() {
        // Arrange
        UUID userId = UUID.randomUUID();

        // Act
        String token = jwtTokenProvider.generateAccessToken(userId, "user@example.com", UserRole.ADMIN);

        // Assert
        assertThat(token).isNotBlank();
        Claims claims = jwtTokenProvider.validateAndGetClaims(token);
        assertThat(claims).isNotNull();
        assertThat(claims.getSubject()).isEqualTo(userId.toString());
        assertThat(claims.get("email", String.class)).isEqualTo("user@example.com");
        assertThat(claims.get("role", String.class)).isEqualTo("ADMIN");
    }

    @Test
    void generateRefreshToken_shouldReturnNonBlankUuidString() {
        // Act
        String refreshToken = jwtTokenProvider.generateRefreshToken();

        // Assert
        assertThat(refreshToken).isNotBlank();
        assertThatCode(() -> UUID.fromString(refreshToken)).doesNotThrowAnyException();
    }

    @Test
    void validateAndGetClaims_shouldReturnNull_forTamperedToken() {
        // Arrange
        UUID userId = UUID.randomUUID();
        String token = jwtTokenProvider.generateAccessToken(userId, "user@example.com", UserRole.USER);
        String tampered = token.substring(0, token.length() - 5) + "XXXXX";

        // Act
        Claims claims = jwtTokenProvider.validateAndGetClaims(tampered);

        // Assert
        assertThat(claims).isNull();
    }

    @Test
    void validateAndGetClaims_shouldReturnNull_forGarbageToken() {
        // Act
        Claims claims = jwtTokenProvider.validateAndGetClaims("not.a.jwt.token");

        // Assert
        assertThat(claims).isNull();
    }

    @Test
    void getRefreshExpirationMs_shouldReturnConfiguredValue() {
        assertThat(jwtTokenProvider.getRefreshExpirationMs()).isEqualTo(REFRESH_EXPIRATION_MS);
    }
}
