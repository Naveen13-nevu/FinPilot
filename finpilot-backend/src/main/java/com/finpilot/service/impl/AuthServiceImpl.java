package com.finpilot.service.impl;

import com.finpilot.dto.request.ForgotPasswordRequest;
import com.finpilot.dto.request.LoginRequest;
import com.finpilot.dto.request.RefreshTokenRequest;
import com.finpilot.dto.request.RegisterRequest;
import com.finpilot.dto.request.ResetPasswordRequest;
import com.finpilot.dto.response.AuthResponse;
import com.finpilot.entity.PasswordResetToken;
import com.finpilot.entity.RefreshToken;
import com.finpilot.entity.Role;
import com.finpilot.entity.User;
import com.finpilot.exception.BadRequestException;
import com.finpilot.exception.DuplicateResourceException;
import com.finpilot.exception.InvalidTokenException;
import com.finpilot.exception.ResourceNotFoundException;
import com.finpilot.mapper.UserMapper;
import com.finpilot.repository.PasswordResetTokenRepository;
import com.finpilot.repository.RefreshTokenRepository;
import com.finpilot.repository.UserRepository;
import com.finpilot.security.JwtService;
import com.finpilot.service.AuthService;
import com.finpilot.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;
    private final EmailService emailService;

    @Value("${app.jwt.refresh-token-expiration-ms}")
    private long refreshTokenExpirationMs;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Value("${app.frontend.reset-password-path}")
    private String resetPasswordPath;

    @Value("${app.password-reset.token-expiration-minutes}")
    private long passwordResetExpirationMinutes;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("An account with this email already exists");
        }

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail().toLowerCase())
                .password(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .role(Role.ROLE_USER)
                .build();

        user = userRepository.save(user);
        emailService.sendWelcomeEmail(user.getEmail(), user.getFirstName());

        return buildAuthResponse(user);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        User user = userRepository.findByEmail(request.getEmail().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", request.getEmail()));

        return buildAuthResponse(user);
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String requestToken = request.getRefreshToken();

        RefreshToken storedToken = refreshTokenRepository.findByToken(requestToken)
                .orElseThrow(() -> new InvalidTokenException("Refresh token is invalid"));

        if (storedToken.getRevoked() || storedToken.isExpired()) {
            throw new InvalidTokenException("Refresh token has expired or been revoked. Please log in again");
        }

        User user = storedToken.getUser();

        String newAccessToken = jwtService.generateAccessToken(user);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(storedToken.getToken())
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessTokenExpirationMs())
                .user(userMapper.toUserResponse(user))
                .build();
    }

    @Override
    @Transactional
    public void logout(RefreshTokenRequest request) {
        refreshTokenRepository.findByToken(request.getRefreshToken())
                .ifPresent(token -> {
                    token.setRevoked(true);
                    refreshTokenRepository.save(token);
                });
    }

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail().toLowerCase())
                .orElse(null);

        // Do not reveal whether the email exists - respond the same way either way
        if (user == null) {
            return;
        }

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiryDate(Instant.now().plus(passwordResetExpirationMinutes, ChronoUnit.MINUTES))
                .used(false)
                .build();

        passwordResetTokenRepository.save(resetToken);

        String resetLink = frontendUrl + resetPasswordPath + "?token=" + token;
        emailService.sendPasswordResetEmail(user.getEmail(), user.getFirstName(), resetLink);
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new InvalidTokenException("Password reset token is invalid"));

        if (resetToken.getUsed() || resetToken.isExpired()) {
            throw new InvalidTokenException("Password reset token has expired or already been used");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        refreshTokenRepository.revokeAllByUser(user);
    }

    private AuthResponse buildAuthResponse(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshTokenValue = jwtService.generateRefreshToken(user);

        RefreshToken refreshToken = RefreshToken.builder()
                .token(refreshTokenValue)
                .user(user)
                .expiryDate(Instant.now().plusMillis(refreshTokenExpirationMs))
                .revoked(false)
                .build();

        refreshTokenRepository.save(refreshToken);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenValue)
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessTokenExpirationMs())
                .user(userMapper.toUserResponse(user))
                .build();
    }
}
