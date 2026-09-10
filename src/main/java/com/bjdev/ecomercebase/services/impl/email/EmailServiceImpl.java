package com.bjdev.ecomercebase.services.impl.email;

import com.bjdev.ecomercebase.services.interfaces.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:no-reply@bjdev.com}")
    private String from;

    @Value("${app.frontend.base-url:http://localhost:3000}")
    private String frontendBaseUrl;

    @Override
    public void sendVerificationEmail(String to, String rawToken) {
        String link = frontendBaseUrl + "/verify-email?token=" + rawToken;
        send(to, "Verifica tu correo",
                "Bienvenido. Para activar tu cuenta, visita el siguiente enlace (expira en 24 horas):\n" + link);
    }

    @Override
    public void sendPasswordResetEmail(String to, String rawToken) {
        String link = frontendBaseUrl + "/reset-password?token=" + rawToken;
        send(to, "Restablece tu contraseña",
                "Solicitaste restablecer tu contraseña. Visita el siguiente enlace (expira en 1 hora):\n" + link
                        + "\n\nSi no fuiste tú, ignora este correo.");
    }

    @Override
    public void sendAdminInvitationEmail(String to, String rawToken) {
        String link = frontendBaseUrl + "/admin/accept-invitation?token=" + rawToken;
        send(to, "Invitación de administrador",
                "Fuiste invitado a ser administrador. Visita el siguiente enlace (expira en 24 horas):\n" + link);
    }

    @Override
    public void sendAdminActionConfirmationEmail(String to, String rawToken, String actionLabel) {
        String link = frontendBaseUrl + "/admin/confirm-action?token=" + rawToken;
        send(to, "Confirma una acción administrativa",
                "Se solicitó " + actionLabel + " desde el panel de administración.\n"
                        + "Si fuiste tú, confirma la acción visitando el siguiente enlace (expira en 15 minutos):\n" + link
                        + "\n\nSi no fuiste tú, ignora este correo y revisa la seguridad de tu cuenta.");
    }

    @Override
    public void sendPlainEmail(String to, String subject, String body) {
        send(to, subject, body);
    }

    @Override
    public void sendOrderOversoldCustomerEmail(String to, Long orderId) {
        send(to, "Actualización sobre tu pedido",
                "Tu pago para el pedido #" + orderId + " fue confirmado, pero uno o más productos no tienen "
                        + "stock disponible. Nuestro equipo se pondrá en contacto contigo en breve para resolverlo.");
    }

    @Override
    public void sendOrderOversoldAdminAlert(String to, Long orderId, String affectedVariantIds) {
        send(to, "ALERTA: sobreventa detectada",
                "El pedido #" + orderId + " se confirmó con pago aprobado pero excede el stock disponible para "
                        + "las variantes: " + affectedVariantIds + ". Requiere revisión manual.");
    }

    private void send(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        try {
            mailSender.send(message);
        } catch (Exception e) {
            // Delivery failures shouldn't fail the calling request (e.g. registration should still succeed).
            log.error("Error sending email to {}: {}", to, e.getMessage());
        }
    }
}
