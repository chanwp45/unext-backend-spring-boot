package com.unext.backend.user.service;

import com.unext.backend.shared.exception.AppException;
import com.unext.backend.user.dto.CreateUserRequest;
import com.unext.backend.user.dto.UserResponse;
import com.unext.backend.user.entity.User;
import com.unext.backend.user.entity.UserRole;
import com.unext.backend.user.exception.UserNotFoundException;
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
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private UserService userService;

    private User existingUser;

    @BeforeEach
    void setUp() {
        existingUser = new User();
        existingUser.setEmail("test@example.com");
        existingUser.setPasswordHash("hashed");
        existingUser.setRole(UserRole.USER);
    }

    @Test
    void create_shouldReturnUserResponse_whenEmailIsNew() {
        var request = new CreateUserRequest("new@example.com", "password123", UserRole.USER);
        given(userRepository.existsByEmail("new@example.com")).willReturn(false);
        given(passwordEncoder.encode("password123")).willReturn("hashed_pw");
        given(userRepository.save(any(User.class))).willAnswer(inv -> inv.getArgument(0));

        UserResponse result = userService.create(request);

        assertThat(result.email()).isEqualTo("new@example.com");
        assertThat(result.role()).isEqualTo(UserRole.USER);
        then(userRepository).should().save(any(User.class));
    }

    @Test
    void create_shouldThrowConflict_whenEmailAlreadyExists() {
        var request = new CreateUserRequest("test@example.com", "password123", UserRole.USER);
        given(userRepository.existsByEmail("test@example.com")).willReturn(true);

        assertThatThrownBy(() -> userService.create(request))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("already registered");
    }

    @Test
    void findById_shouldReturnUser_whenExists() {
        UUID id = UUID.randomUUID();
        given(userRepository.findById(id)).willReturn(Optional.of(existingUser));

        UserResponse result = userService.findById(id);

        assertThat(result.email()).isEqualTo("test@example.com");
    }

    @Test
    void findById_shouldThrowNotFound_whenUserMissing() {
        UUID id = UUID.randomUUID();
        given(userRepository.findById(id)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findById(id))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void delete_shouldSetDeletedAt_whenUserExists() {
        UUID id = UUID.randomUUID();
        given(userRepository.findById(id)).willReturn(Optional.of(existingUser));
        given(userRepository.save(any(User.class))).willAnswer(inv -> inv.getArgument(0));

        userService.delete(id);

        assertThat(existingUser.getDeletedAt()).isNotNull();
    }
}
