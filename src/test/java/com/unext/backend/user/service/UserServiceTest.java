package com.unext.backend.user.service;

import com.unext.backend.shared.exception.AppException;
import com.unext.backend.shared.response.PaginationMeta;
import com.unext.backend.user.dto.CreateUserRequest;
import com.unext.backend.user.dto.UpdateUserRequest;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
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

    @Test
    void delete_shouldThrowNotFound_whenUserMissing() {
        UUID id = UUID.randomUUID();
        given(userRepository.findById(id)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.delete(id))
                .isInstanceOf(UserNotFoundException.class);
    }

    // ─── update ─────────────────────────────────────────────────────────────

    @Test
    void update_shouldUpdatePassword_whenPasswordProvided() {
        UUID id = UUID.randomUUID();
        var req = new UpdateUserRequest("newpassword123", null, null);
        given(userRepository.findById(id)).willReturn(Optional.of(existingUser));
        given(passwordEncoder.encode("newpassword123")).willReturn("new_hashed");
        given(userRepository.save(any(User.class))).willAnswer(inv -> inv.getArgument(0));

        UserResponse result = userService.update(id, req);

        assertThat(existingUser.getPasswordHash()).isEqualTo("new_hashed");
        assertThat(result).isNotNull();
    }

    @Test
    void update_shouldUpdateRole_whenRoleProvided() {
        UUID id = UUID.randomUUID();
        var req = new UpdateUserRequest(null, UserRole.ADMIN, null);
        given(userRepository.findById(id)).willReturn(Optional.of(existingUser));
        given(userRepository.save(any(User.class))).willAnswer(inv -> inv.getArgument(0));

        userService.update(id, req);

        assertThat(existingUser.getRole()).isEqualTo(UserRole.ADMIN);
    }

    @Test
    void update_shouldDeactivateUser_whenActiveSetToFalse() {
        UUID id = UUID.randomUUID();
        var req = new UpdateUserRequest(null, null, false);
        given(userRepository.findById(id)).willReturn(Optional.of(existingUser));
        given(userRepository.save(any(User.class))).willAnswer(inv -> inv.getArgument(0));

        userService.update(id, req);

        assertThat(existingUser.isActive()).isFalse();
    }

    @Test
    void update_shouldThrowNotFound_whenUserMissing() {
        UUID id = UUID.randomUUID();
        var req = new UpdateUserRequest(null, null, null);
        given(userRepository.findById(id)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.update(id, req))
                .isInstanceOf(UserNotFoundException.class);
    }

    // ─── findAll ─────────────────────────────────────────────────────────────

    @Test
    void findAll_shouldReturnPagedUsers() {
        var page = new PageImpl<>(List.of(existingUser), PageRequest.of(0, 20), 1);
        given(userRepository.findAllByActiveTrue(any())).willReturn(page);

        var result = userService.findAll(1, 20);

        assertThat(result.data()).hasSize(1);
        assertThat(result.data().get(0).email()).isEqualTo("test@example.com");
        assertThat(result.pagination().total()).isEqualTo(1L);
    }

    @Test
    void findAll_shouldReturnEmptyPage_whenNoUsers() {
        var emptyPage = new PageImpl<User>(List.of(), PageRequest.of(0, 20), 0);
        given(userRepository.findAllByActiveTrue(any())).willReturn(emptyPage);

        var result = userService.findAll(1, 20);

        assertThat(result.data()).isEmpty();
        assertThat(result.pagination().total()).isZero();
    }
}
