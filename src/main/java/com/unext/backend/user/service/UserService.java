package com.unext.backend.user.service;

import com.unext.backend.shared.exception.AppException;
import com.unext.backend.shared.response.PaginationMeta;
import com.unext.backend.user.dto.CreateUserRequest;
import com.unext.backend.user.dto.UpdateUserRequest;
import com.unext.backend.user.dto.UserResponse;
import com.unext.backend.user.entity.User;
import com.unext.backend.user.exception.UserNotFoundException;
import com.unext.backend.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Creates a new user. Throws 409 if email is already taken.
     */
    @Transactional
    public UserResponse create(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new AppException("EMAIL_CONFLICT",
                    "Email '" + request.email() + "' is already registered.", HttpStatus.CONFLICT);
        }

        User user = new User();
        user.setEmail(request.email().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(request.role());

        return UserResponse.from(userRepository.save(user));
    }

    /**
     * Returns a paginated list of active users.
     */
    @Transactional(readOnly = true)
    public PageResult<UserResponse> findAll(int page, int limit) {
        PageRequest pageRequest = PageRequest.of(page - 1, limit, Sort.by("createdAt").descending());
        Page<User> result = userRepository.findAllByActiveTrue(pageRequest);
        List<UserResponse> data = result.getContent().stream().map(UserResponse::from).toList();
        return new PageResult<>(data, PaginationMeta.of(page, limit, result.getTotalElements()));
    }

    /**
     * Returns a single user by ID.
     */
    @Transactional(readOnly = true)
    public UserResponse findById(UUID id) {
        return userRepository.findById(id)
                .map(UserResponse::from)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    /**
     * Updates allowed fields on a user.
     */
    @Transactional
    public UserResponse update(UUID id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        if (request.password() != null) {
            user.setPasswordHash(passwordEncoder.encode(request.password()));
        }
        if (request.role() != null) {
            user.setRole(request.role());
        }
        if (request.active() != null) {
            user.setActive(request.active());
        }

        return UserResponse.from(userRepository.save(user));
    }

    /**
     * Soft-deletes a user by setting deletedAt.
     */
    @Transactional
    public void delete(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        user.setDeletedAt(Instant.now());
        userRepository.save(user);
    }

    public record PageResult<T>(List<T> data, PaginationMeta pagination) {}
}

