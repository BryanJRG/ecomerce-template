package com.bjdev.ecomercebase.controllers;

import com.bjdev.ecomercebase.dto.request.AcceptAdminInvitationRequest;
import com.bjdev.ecomercebase.dto.request.ChangePasswordRequest;
import com.bjdev.ecomercebase.dto.request.ForgotPasswordRequest;
import com.bjdev.ecomercebase.dto.request.LoginRequest;
import com.bjdev.ecomercebase.dto.request.RegisterRequest;
import com.bjdev.ecomercebase.dto.request.RejectAdminInvitationRequest;
import com.bjdev.ecomercebase.dto.request.ResendVerificationEmailRequest;
import com.bjdev.ecomercebase.dto.request.ResetPasswordRequest;
import com.bjdev.ecomercebase.dto.response.MessageResponse;
import com.bjdev.ecomercebase.dto.response.UserResponse;
import com.bjdev.ecomercebase.services.impl.admin.AdminActionDispatchService;
import com.bjdev.ecomercebase.services.interfaces.AdminInvitationService;
import com.bjdev.ecomercebase.services.interfaces.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final AdminInvitationService adminInvitationService;
    private final AdminActionDispatchService adminActionDispatchService;

    @PostMapping("/register")
    public ResponseEntity<MessageResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @GetMapping("/verify-email")
    public ResponseEntity<MessageResponse> verifyEmail(@RequestParam String token) {
        return ResponseEntity.ok(authService.verifyEmail(token));
    }

    @PostMapping("/resend-verification-email")
    public ResponseEntity<MessageResponse> resendVerificationEmail(@Valid @RequestBody ResendVerificationEmailRequest request) {
        return ResponseEntity.ok(authService.resendVerificationEmail(request));
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(@Valid @RequestBody LoginRequest request,
                                               HttpServletRequest httpRequest,
                                               HttpServletResponse httpResponse) {
        return ResponseEntity.ok(authService.login(request, httpRequest, httpResponse));
    }

    @PostMapping("/refresh")
    public ResponseEntity<Void> refresh(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        authService.refresh(httpRequest, httpResponse);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        authService.logout(httpRequest, httpResponse);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return ResponseEntity.ok(authService.forgotPassword(request));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return ResponseEntity.ok(authService.resetPassword(request));
    }

    @PostMapping("/change-password")
    public ResponseEntity<MessageResponse> changePassword(@Valid @RequestBody ChangePasswordRequest request,
                                                            HttpServletRequest httpRequest,
                                                            HttpServletResponse httpResponse) {
        return ResponseEntity.ok(authService.changePassword(request, httpRequest, httpResponse));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me() {
        return ResponseEntity.ok(authService.getCurrentUser());
    }

    // Public on purpose: the invitee isn't an admin yet and may not reach the Cloudflare-gated /api/admin/** routes.
    @GetMapping("/admin-invitations/validate/{token}")
    public ResponseEntity<MessageResponse> validateInvitation(@PathVariable String token) {
        adminInvitationService.validateInvitation(token);
        return ResponseEntity.ok(new MessageResponse("Invitación válida."));
    }

    // Requires authentication (see SecurityConfig): the invitee must log in to their existing
    // account before the invitation can be applied to it.
    @PostMapping("/admin-invitations/accept")
    public ResponseEntity<MessageResponse> acceptInvitation(@Valid @RequestBody AcceptAdminInvitationRequest request) {
        return ResponseEntity.ok(adminInvitationService.acceptInvitation(request));
    }

    // Public: declining doesn't touch any account, so no auth is required to do it.
    @PostMapping("/admin-invitations/reject")
    public ResponseEntity<MessageResponse> rejectInvitation(@Valid @RequestBody RejectAdminInvitationRequest request) {
        return ResponseEntity.ok(adminInvitationService.rejectInvitation(request));
    }

    // Public and token-driven on purpose (see AdminActionConfirmationService): this must keep
    // working even if the admin session/JWT that requested the action was compromised, since it
    // proves control of the admin's own email instead of relying on that session.
    @PostMapping("/admin-actions/confirm")
    public ResponseEntity<MessageResponse> confirmAdminAction(@RequestParam String token) {
        return ResponseEntity.ok(adminActionDispatchService.confirmAndExecute(token));
    }
}
