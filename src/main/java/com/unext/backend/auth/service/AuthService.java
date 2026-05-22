package com.unext.backend.auth.service;

import com.unext.backend.auth.dto.LoginRequest;
import com.unext.backend.auth.dto.LoginResponse;
import com.unext.backend.auth.entity.RefreshToken;
import com.unext.backend.auth.repository.RefreshTokenRepository;
import com.unext.backend.shared.exception.AppException;
import com.unext.backend.shared.security.JwtTokenProvider;
import com.unext.backend.user.entity.User;
import com.unext.backend.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;

@Service
public class AuthService {

    private static final int MAX_LOGIN_ATTEMPTS = 5;

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            JwtTokenProvider jwtTokenProvider,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Authenticates a user and returns access + refresh tokens.
     */
    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email().toLowerCase())
                .filter(User::isActive)
                .orElseThrow(() -> new AppException(
                        "INVALID_CREDENTIALS", "Invalid email or password.", HttpStatus.UNAUTHORIZED));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new AppException("INVALID_CREDENTIALS", "Invalid email or password.", HttpStatus.UNAUTHORIZED);
        }

        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail(), user.getRole());
        String rawRefresh = jwtTokenProvider.generateRefreshToken();
        String refreshHash = sha256(rawRefresh);

        RefreshToken rt = new RefreshToken();
        rt.setUser(user);
        rt.setTokenHash(refreshHash);
        rt.setExpiresAt(Instant.now().plusMillis(jwtTokenProvider.getRefreshExpirationMs()));
        refreshTokenRepository.save(rt);

        return LoginResponse.of(accessToken, rawRefresh, jwtTokenProvider.getRefreshExpirationMs());
    }

    /**
     * Issues a new access token using a valid, non-expired refresh token (rotation).
     */
    @Transactional
    public LoginResponse refresh(String rawRefreshToken) {
        String hash = sha256(rawRefreshToken);

        RefreshToken rt = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new AppException(
                        "INVALID_TOKEN", "Refresh token is invalid.", HttpStatus.UNAUTHORIZED));

        if (rt.isRevoked() || rt.getExpiresAt().isBefore(Instant.now())) {
            throw new AppException("INVALID_TOKEN", "Refresh token has expired or been revoked.", HttpStatus.UNAUTHORIZED);
        }

        rt.setRevoked(true);
        refreshTokenRepository.save(rt);

        User user = rt.getUser();
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail(), user.getRole());
        String newRawRefresh = jwtTokenProvider.generateRefreshToken();
        String newHash = sha256(newRawRefresh);

        RefreshToken newRt = new RefreshToken();
        newRt.setUser(user);
        newRt.setTokenHash(newHash);
        newRt.setExpiresAt(Instant.now().plusMillis(jwtTokenProvider.getRefreshExpirationMs()));
        refreshTokenRepository.save(newRt);

        return LoginResponse.of(accessToken, newRawRefresh, jwtTokenProvider.getRefreshExpirationMs());
    }

    /**
     * Revokes all refresh tokens for the authenticated user (logout).
     */
    @Transactional
    public void logout(String userId) {
        try {
            refreshTokenRepository.revokeAllByUserId(java.util.UUID.fromString(userId));
        } catch (IllegalArgumentException ex) {
            throw new AppException("INVALID_USER", "Invalid user ID format.", HttpStatus.BAD_REQUEST);
        }
    }

    private static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }
}

