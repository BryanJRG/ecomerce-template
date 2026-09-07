package com.bjdev.base.services.interfaces;

import com.bjdev.base.dto.request.ChangePasswordRequest;
import com.bjdev.base.dto.request.ForgotPasswordRequest;
import com.bjdev.base.dto.request.LoginRequest;
import com.bjdev.base.dto.request.RegisterRequest;
import com.bjdev.base.dto.request.ResendVerificationEmailRequest;
import com.bjdev.base.dto.request.ResetPasswordRequest;
import com.bjdev.base.dto.response.MessageResponse;
import com.bjdev.base.dto.response.UserResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {

    MessageResponse register(RegisterRequest request);

    UserResponse login(LoginRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse);

    void refresh(HttpServletRequest httpRequest, HttpServletResponse httpResponse);

    void logout(HttpServletRequest httpRequest, HttpServletResponse httpResponse);

    MessageResponse verifyEmail(String token);

    MessageResponse resendVerificationEmail(ResendVerificationEmailRequest request);

    MessageResponse forgotPassword(ForgotPasswordRequest request);

    MessageResponse resetPassword(ResetPasswordRequest request);

    MessageResponse changePassword(ChangePasswordRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse);

    UserResponse getCurrentUser();
}
