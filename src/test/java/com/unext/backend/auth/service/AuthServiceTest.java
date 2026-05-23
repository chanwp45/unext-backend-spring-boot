package com.unext.backend.auth.service;

import com.unext.backend.auth.dto.LoginRequest;
import com.unext.backend.auth.dto.LoginResponse;
import com.unext.backend.auth.entity.RefreshToken;
import com.unext.backend.auth.repository.RefreshTokenRepository;
import com.unext.backend.shared.exception.AppException;
import com.unext.backend.shared.security.JwtTokenProvider;
import com.unext.backend.user.entity.User;
import com.unext.backend.user.entity.UserRole;
import com.unext.backend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private AuthService authService;

    private User activeUser;

    @BeforeEach
    void setUp() {
        activeUser = new User();
        activeUser.setEmail("user@example.com");
        activeUser.setPasswordHash("hashed");
        activeUser.setRole(UserRole.USER);
        activeUser.setActive(true);
    }

    @Test
    void login_shouldReturnTokens_whenCredentialsAreValid() {
        given(userRepository.findByEmail("user@example.com")).willReturn(Optional.of(activeUser));
        given(passwordEncoder.matches("pass1234", "hashed")).willReturn(true);
        given(jwtTokenProvider.generateAccessToken(any(), anyString(), any())).willReturn("access.token.here");
        given(jwtTokenProvider.generateRefreshToken()).willReturn("raw-refresh-uuid");
        given(jwtTokenProvider.getRefreshExpirationMs()).willReturn(604800000L);
        given(refreshTokenRepository.save(any(RefreshToken.class))).willAnswer(inv -> inv.getArgument(0));

        LoginResponse response = authService.login(new LoginRequest("user@example.com", "pass1234"));

        assertThat(response.accessToken()).isEqualTo("access.token.here");
        assertThat(response.tokenType()).isEqualTo("Bearer");
    }

    @Test
    void login_shouldThrow_whenPasswordIsWrong() {
        given(userRepository.findByEmail("user@example.com")).willReturn(Optional.of(activeUser));
        given(passwordEncoder.matches(anyString(), anyString())).willReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequest("user@example.com", "wrongpass")))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Invalid email or password");
    }

    @Test
    void login_shouldThrow_whenUserNotFound() {
        given(userRepository.findByEmail(anyString())).willReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequest("nobody@example.com", "pass")))
                .isInstanceOf(AppException.class);
    }

    @Test
    void login_shouldThrow_whenUserIsInactive() {
        activeUser.setActive(false);
        given(userRepository.findByEmail("user@example.com")).willReturn(Optional.of(activeUser));

        assertThatThrownBy(() -> authService.login(new LoginRequest("user@example.com", "pass1234")))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", "INVALID_CREDENTIALS");
    }

    // ─── refresh ────────────────────────────────────────────────────────────

    @Test
    void refresh_shouldReturnNewTokens_whenRefreshTokenIsValid() {
        RefreshToken rt = new RefreshToken();
        rt.setUser(activeUser);
        rt.setRevoked(false);
        rt.setExpiresAt(Instant.now().plusSeconds(3600));

        given(refreshTokenRepository.findByTokenHash(anyString())).willReturn(Optional.of(rt));
        given(refreshTokenRepository.save(any(RefreshToken.class))).willAnswer(inv -> inv.getArgument(0));
        given(jwtTokenProvider.generateAccessToken(any(), anyString(), any())).willReturn("new.access.token");
        given(jwtTokenProvider.generateRefreshToken()).willReturn("new-raw-refresh");
        given(jwtTokenProvider.getRefreshExpirationMs()).willReturn(604800000L);

        LoginResponse response = authService.refresh("any-raw-token");

        assertThat(response.accessToken()).isEqualTo("new.access.token");
        assertThat(response.refreshToken()).isEqualTo("new-raw-refresh");
        assertThat(rt.isRevoked()).isTrue();
    }

    @Test
    void refresh_shouldThrow_whenRefreshTokenNotFound() {
        given(refreshTokenRepository.findByTokenHash(anyString())).willReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refresh("unknown-token"))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", "INVALID_TOKEN");
    }

    @Test
    void refresh_shouldThrow_whenRefreshTokenIsRevoked() {
        RefreshToken rt = new RefreshToken();
        rt.setUser(activeUser);
        rt.setRevoked(true);
        rt.setExpiresAt(Instant.now().plusSeconds(3600));

        given(refreshTokenRepository.findByTokenHash(anyString())).willReturn(Optional.of(rt));

        assertThatThrownBy(() -> authService.refresh("revoked-token"))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", "INVALID_TOKEN");
    }

    @Test
    void refresh_shouldThrow_whenRefreshTokenIsExpired() {
        RefreshToken rt = new RefreshToken();
        rt.setUser(activeUser);
        rt.setRevoked(false);
        rt.setExpiresAt(Instant.now().minusSeconds(1));

        given(refreshTokenRepository.findByTokenHash(anyString())).willReturn(Optional.of(rt));

        assertThatThrownBy(() -> authService.refresh("expired-token"))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", "INVALID_TOKEN");
    }

    // ─── logout ─────────────────────────────────────────────────────────────

    @Test
    void logout_shouldRevokeAllTokens_whenUserIdIsValid() {
        UUID userId = UUID.randomUUID();
        willDoNothing().given(refreshTokenRepository).revokeAllByUserId(userId);

        authService.logout(userId.toString());

        then(refreshTokenRepository).should().revokeAllByUserId(userId);
    }

    @Test
    void logout_shouldThrow_whenUserIdIsInvalidFormat() {
        assertThatThrownBy(() -> authService.logout("not-a-uuid"))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", "INVALID_USER");
    }
}
