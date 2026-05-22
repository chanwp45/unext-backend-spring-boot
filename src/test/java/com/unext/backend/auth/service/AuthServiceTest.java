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
}
