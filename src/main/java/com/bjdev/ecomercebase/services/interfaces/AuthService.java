package com.bjdev.ecomercebase.services.interfaces;

import com.bjdev.ecomercebase.dto.request.ChangePasswordRequest;
import com.bjdev.ecomercebase.dto.request.ForgotPasswordRequest;
import com.bjdev.ecomercebase.dto.request.LoginRequest;
import com.bjdev.ecomercebase.dto.request.RegisterRequest;
import com.bjdev.ecomercebase.dto.request.ResendVerificationEmailRequest;
import com.bjdev.ecomercebase.dto.request.ResetPasswordRequest;
import com.bjdev.ecomercebase.dto.response.MessageResponse;
import com.bjdev.ecomercebase.dto.response.UserResponse;
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
