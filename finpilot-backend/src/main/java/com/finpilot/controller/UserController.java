package com.finpilot.controller;

import com.finpilot.dto.request.ChangePasswordRequest;
import com.finpilot.dto.request.UpdateProfileRequest;
import com.finpilot.dto.response.ApiResponse;
import com.finpilot.dto.response.UserResponse;
import com.finpilot.entity.User;
import com.finpilot.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Profile", description = "Endpoints for viewing and updating the authenticated user's profile")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Get the currently authenticated user's profile")
    public ApiResponse<UserResponse> getCurrentUser(@AuthenticationPrincipal User currentUser) {
        return ApiResponse.success(userService.getCurrentUserProfile(currentUser));
    }

    @PutMapping("/me")
    @Operation(summary = "Update the currently authenticated user's profile")
    public ApiResponse<UserResponse> updateProfile(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody UpdateProfileRequest request) {
        return ApiResponse.success("Profile updated successfully", userService.updateProfile(currentUser, request));
    }

    @PutMapping("/me/password")
    @Operation(summary = "Change the currently authenticated user's password")
    public ApiResponse<Void> changePassword(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(currentUser, request);
        return ApiResponse.message("Password changed successfully");
    }
}
