package com.finpilot.service.impl;

import com.finpilot.dto.request.ChangePasswordRequest;
import com.finpilot.dto.request.UpdateProfileRequest;
import com.finpilot.dto.response.UserResponse;
import com.finpilot.entity.User;
import com.finpilot.exception.BadRequestException;
import com.finpilot.mapper.UserMapper;
import com.finpilot.repository.RefreshTokenRepository;
import com.finpilot.repository.UserRepository;
import com.finpilot.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Override
    public UserResponse getCurrentUserProfile(User currentUser) {
        return userMapper.toUserResponse(currentUser);
    }

    @Override
    @Transactional
    public UserResponse updateProfile(User currentUser, UpdateProfileRequest request) {
        if (StringUtils.hasText(request.getFirstName())) {
            currentUser.setFirstName(request.getFirstName());
        }
        if (StringUtils.hasText(request.getLastName())) {
            currentUser.setLastName(request.getLastName());
        }
        if (request.getPhoneNumber() != null) {
            currentUser.setPhoneNumber(request.getPhoneNumber());
        }
        if (StringUtils.hasText(request.getCurrency())) {
            currentUser.setCurrency(request.getCurrency());
        }
        if (request.getMonthlyBudget() != null) {
            currentUser.setMonthlyBudget(request.getMonthlyBudget());
        }
        if (request.getDarkModeEnabled() != null) {
            currentUser.setDarkModeEnabled(request.getDarkModeEnabled());
        }

        User updated = userRepository.save(currentUser);
        return userMapper.toUserResponse(updated);
    }

    @Override
    @Transactional
    public void changePassword(User currentUser, ChangePasswordRequest request) {
        if (!passwordEncoder.matches(request.getCurrentPassword(), currentUser.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }

        currentUser.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(currentUser);

        // Revoke all sessions so the user must log in again with the new password
        refreshTokenRepository.revokeAllByUser(currentUser);
    }
}
