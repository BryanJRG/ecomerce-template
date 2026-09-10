package com.bjdev.ecomercebase.services.interfaces;

public interface EmailService {
    void sendVerificationEmail(String to, String rawToken);

    void sendPasswordResetEmail(String to, String rawToken);

    void sendAdminInvitationEmail(String to, String rawToken);

    void sendAdminActionConfirmationEmail(String to, String rawToken, String actionLabel);

    /** Generic plain-text email for internal/admin notifications — see EntityChangeNotificationListener. */
    void sendPlainEmail(String to, String subject, String body);

    void sendOrderOversoldCustomerEmail(String to, Long orderId);

    void sendOrderOversoldAdminAlert(String to, Long orderId, String affectedVariantIds);
}
