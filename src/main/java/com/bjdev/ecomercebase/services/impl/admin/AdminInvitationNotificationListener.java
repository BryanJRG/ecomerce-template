package com.bjdev.ecomercebase.services.impl.admin;

import com.bjdev.ecomercebase.events.AdminInvitationAcceptedEvent;
import com.bjdev.ecomercebase.events.AdminInvitationCreatedEvent;
import com.bjdev.ecomercebase.events.AdminInvitationRejectedEvent;
import com.bjdev.ecomercebase.models.notification.NotificationType;
import com.bjdev.ecomercebase.models.user.User;
import com.bjdev.ecomercebase.repositories.user.UserRepository;
import com.bjdev.ecomercebase.services.interfaces.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Map;

/**
 * Translates admin-invitation domain events into the default notifications for that feature.
 * This is the "glue" layer: NotificationService knows nothing about invitations, and
 * AdminInvitationServiceImpl knows nothing about notifications — each event handler here is free
 * to be added, removed or changed without touching either side. Other modules/variants should
 * follow the same pattern: publish your own event, add your own listener here-equivalent.
 *
 * AFTER_COMMIT: notifications are a secondary effect and must only be created once the invitation
 * action they describe has actually persisted — never before, and never as a reason to roll it back.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminInvitationNotificationListener {

    private static final String ADMIN_ROLE_NAME = "ADMIN";
    private static final String RELATED_TABLE = "admin_invitations";

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onInvitationCreated(AdminInvitationCreatedEvent event) {
        runSafely(() -> {
            User invitee = findUser(event.invitedUserId());
            User creator = findUser(event.createdByUserId());
            Map<String, Object> data = Map.of("invitationId", event.invitationId());

            if (invitee != null) {
                notificationService.notifyUser(invitee, creator,
                        NotificationType.ADMIN_INVITATION_CREATED,
                        "Invitación de administrador",
                        "Fuiste invitado a convertirte en administrador. Revisa tu correo para aceptar o rechazar la invitación.",
                        RELATED_TABLE, event.invitationId(), data);
            }

            String creatorLabel = creator != null ? creator.getEmail() : "Un administrador";
            notificationService.notifyRole(ADMIN_ROLE_NAME, creator,
                    NotificationType.ADMIN_INVITATION_CREATED,
                    "Nueva invitación de administrador enviada",
                    creatorLabel + " invitó a " + event.invitedEmail() + " a convertirse en administrador.",
                    RELATED_TABLE, event.invitationId(), data);
        });
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onInvitationAccepted(AdminInvitationAcceptedEvent event) {
        runSafely(() -> {
            User creator = findUser(event.createdByUserId());
            User acceptedBy = findUser(event.acceptedByUserId());
            Map<String, Object> data = Map.of("invitationId", event.invitationId());

            if (creator != null) {
                notificationService.notifyUser(creator, acceptedBy,
                        NotificationType.ADMIN_INVITATION_ACCEPTED,
                        "Invitación aceptada",
                        event.acceptedByEmail() + " aceptó tu invitación y ahora es administrador.",
                        RELATED_TABLE, event.invitationId(), data);
            }

            notificationService.notifyRole(ADMIN_ROLE_NAME, acceptedBy,
                    NotificationType.ADMIN_INVITATION_ACCEPTED,
                    "Nuevo administrador",
                    event.acceptedByEmail() + " es ahora administrador del sistema.",
                    RELATED_TABLE, event.invitationId(), data);
        });
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onInvitationRejected(AdminInvitationRejectedEvent event) {
        runSafely(() -> {
            User creator = findUser(event.createdByUserId());
            if (creator == null) return;

            notificationService.notifyUser(creator, null,
                    NotificationType.ADMIN_INVITATION_REJECTED,
                    "Invitación rechazada",
                    event.invitedEmail() + " rechazó tu invitación de administrador.",
                    RELATED_TABLE, event.invitationId(), Map.of("invitationId", event.invitationId()));
        });
    }

    private User findUser(Long userId) {
        return userId != null ? userRepository.findById(userId).orElse(null) : null;
    }

    /** Notification delivery must never surface as a failure of the action that triggered it. */
    private void runSafely(Runnable action) {
        try {
            action.run();
        } catch (Exception e) {
            log.error("Failed to create notification(s) for admin invitation event: {}", e.getMessage(), e);
        }
    }
}
