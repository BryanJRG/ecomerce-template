package com.bjdev.ecomercebase.services.impl.admin;

import com.bjdev.ecomercebase.exception.AuthException;
import com.bjdev.ecomercebase.models.auth.PendingActionStatus;
import com.bjdev.ecomercebase.models.auth.PendingActionType;
import com.bjdev.ecomercebase.models.auth.PendingAdminAction;
import com.bjdev.ecomercebase.models.user.User;
import com.bjdev.ecomercebase.repositories.auth.PendingAdminActionRepository;
import com.bjdev.ecomercebase.services.impl.audit.AuditService;
import com.bjdev.ecomercebase.services.interfaces.EmailService;
import com.bjdev.ecomercebase.utils.TokenHasher;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Gates critical admin actions behind an email confirmation step, independent of the requester's
 * JWT session — the token is only usable by whoever controls the requester's email inbox. Deliberately
 * has no dependency on the domain services (AdminInvitationService, etc.) that own the actions
 * themselves, so it can't form a circular dependency with them; see AdminActionDispatchService for
 * where a confirmed action is actually carried out.
 */
@Service
@RequiredArgsConstructor
public class AdminActionConfirmationService {

    private static final int EXPIRATION_MINUTES = 15;

    private final PendingAdminActionRepository pendingAdminActionRepository;
    private final EmailService emailService;
    private final AuditService auditService;

    @Value("${app.admin.action-confirmation.enabled:false}")
    private boolean enabled;

    public boolean isEnabled() {
        return enabled;
    }

    @Transactional
    public void requestConfirmation(PendingActionType type, User requestedBy, String payload, String actionLabel) {
        String rawToken = TokenHasher.generateRandomToken();

        PendingAdminAction action = PendingAdminAction.builder()
                .type(type)
                .payload(payload)
                .requestedBy(requestedBy)
                .tokenHash(TokenHasher.hashToken(rawToken))
                .status(PendingActionStatus.PENDING)
                .expiresAt(LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES))
                .build();
        action = pendingAdminActionRepository.save(action);

        emailService.sendAdminActionConfirmationEmail(requestedBy.getEmail(), rawToken, actionLabel);
        auditService.record(requestedBy, "ADMIN_ACTION_CONFIRMATION_REQUESTED", "pending_admin_actions",
                action.getId(), null, null, null);
    }

    @Transactional
    public PendingAdminAction confirm(String rawToken) {
        PendingAdminAction action = pendingAdminActionRepository.findByTokenHash(TokenHasher.hashToken(rawToken))
                .orElseThrow(AuthException::invalidToken);

        if (action.getStatus() != PendingActionStatus.PENDING || action.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw AuthException.invalidToken();
        }

        action.setStatus(PendingActionStatus.CONFIRMED);
        action.setConfirmedAt(LocalDateTime.now());
        action = pendingAdminActionRepository.save(action);

        auditService.record(action.getRequestedBy(), "ADMIN_ACTION_CONFIRMED", "pending_admin_actions",
                action.getId(), null, null, null);

        return action;
    }
}
