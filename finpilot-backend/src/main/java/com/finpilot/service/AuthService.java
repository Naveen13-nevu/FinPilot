package com.finpilot.service;

import com.finpilot.dto.request.ForgotPasswordRequest;
import com.finpilot.dto.request.LoginRequest;
import com.finpilot.dto.request.RefreshTokenRequest;
import com.finpilot.dto.request.RegisterRequest;
import com.finpilot.dto.request.ResetPasswordRequest;
import com.finpilot.dto.response.AuthResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refreshToken(RefreshTokenRequest request);

    void logout(RefreshTokenRequest request);

    void forgotPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);
}
