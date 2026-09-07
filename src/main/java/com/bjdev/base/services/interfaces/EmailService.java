package com.bjdev.base.services.interfaces;

public interface EmailService {
    void sendVerificationEmail(String to, String rawToken);

    void sendPasswordResetEmail(String to, String rawToken);

    void sendAdminInvitationEmail(String to, String rawToken);

    void sendAdminActionConfirmationEmail(String to, String rawToken, String actionLabel);
}
