package com.finpilot.service;

import com.finpilot.dto.request.ChangePasswordRequest;
import com.finpilot.dto.request.UpdateProfileRequest;
import com.finpilot.dto.response.UserResponse;
import com.finpilot.entity.User;

public interface UserService {

    UserResponse getCurrentUserProfile(User currentUser);

    UserResponse updateProfile(User currentUser, UpdateProfileRequest request);

    void changePassword(User currentUser, ChangePasswordRequest request);
}
