package com.bjdev.ecomercebase.services.impl.admin;

import com.bjdev.ecomercebase.dto.request.AcceptAdminInvitationRequest;
import com.bjdev.ecomercebase.dto.request.CreateAdminInvitationRequest;
import com.bjdev.ecomercebase.dto.request.RejectAdminInvitationRequest;
import com.bjdev.ecomercebase.dto.response.AdminActionResult;
import com.bjdev.ecomercebase.dto.response.AdminInvitationResponse;
import com.bjdev.ecomercebase.dto.response.MessageResponse;
import com.bjdev.ecomercebase.events.AdminInvitationAcceptedEvent;
import com.bjdev.ecomercebase.events.AdminInvitationCreatedEvent;
import com.bjdev.ecomercebase.events.AdminInvitationRejectedEvent;
import com.bjdev.ecomercebase.exception.AuthException;
import com.bjdev.ecomercebase.models.auth.AdminInvitation;
import com.bjdev.ecomercebase.models.auth.InvitationStatus;
import com.bjdev.ecomercebase.models.auth.PendingActionType;
import com.bjdev.ecomercebase.models.user.Role;
import com.bjdev.ecomercebase.models.user.User;
import com.bjdev.ecomercebase.repositories.auth.AdminInvitationRepository;
import com.bjdev.ecomercebase.repositories.user.RoleRepository;
import com.bjdev.ecomercebase.repositories.user.UserRepository;
import com.bjdev.ecomercebase.security.CurrentUserProvider;
import com.bjdev.ecomercebase.services.impl.audit.AuditService;
import com.bjdev.ecomercebase.services.interfaces.AdminInvitationService;
import com.bjdev.ecomercebase.services.interfaces.EmailService;
import com.bjdev.ecomercebase.utils.TokenHasher;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminInvitationServiceImpl implements AdminInvitationService {

    /** Hardcoded on purpose: the client can never choose which role an invitation grants. */
    private static final String ADMIN_ROLE_NAME = "ADMIN";

    private final AdminInvitationRepository adminInvitationRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final EmailService emailService;
    private final AuditService auditService;
    private final CurrentUserProvider currentUserProvider;
    private final ApplicationEventPublisher eventPublisher;
    private final AdminActionConfirmationService adminActionConfirmationService;

    @Override
    @Transactional
    public AdminActionResult<AdminInvitationResponse> createInvitation(CreateAdminInvitationRequest request) {
        User creator = currentUserProvider.getCurrentUser();
        User invitee = validateInvitee(request.email());

        if (adminActionConfirmationService.isEnabled()) {
            adminActionConfirmationService.requestConfirmation(
                    PendingActionType.CREATE_ADMIN_INVITATION, creator, request.email(),
                    "invitar a " + request.email() + " como administrador");
            return AdminActionResult.pending(
                    "Revisa tu correo para confirmar esta acción. La invitación se enviará una vez confirmada.");
        }

        return AdminActionResult.completed(persistInvitation(creator, invitee));
    }

    @Override
    @Transactional
    public AdminInvitationResponse executeCreateInvitation(User creator, String invitedEmail) {
        User requester = validateRequesterStillAdmin(creator.getEmail());
        User invitee = validateInvitee(invitedEmail);
        return persistInvitation(requester, invitee);
    }

    /**
     * The confirmation email can sit unconfirmed for up to 15 minutes (see
     * AdminActionConfirmationService), and the confirm endpoint is deliberately public/token-only —
     * it doesn't require the requester's JWT session at all, by design, so it still works even if
     * that session was revoked. That means it would otherwise still work even if the requester was
     * disabled or lost the ADMIN role in the meantime, defeating the reason this gate exists. Re-check
     * fresh from the DB before actually creating the invitation.
     */
    private User validateRequesterStillAdmin(String requesterEmail) {
        User requester = userRepository.findByEmailWithRolesAndPermisos(requesterEmail)
                .orElseThrow(AuthException::invalidCredentials);
        if (!Boolean.TRUE.equals(requester.getIsActive())) {
            throw AuthException.accountDisabled();
        }
        if (requester.getRoles().stream().noneMatch(r -> ADMIN_ROLE_NAME.equals(r.getName()))) {
            throw AuthException.invalidCredentials();
        }
        return requester;
    }

    /**
     * The invitee must already be a registered, verified user of the system — invitations never
     * create accounts, they only grant the ADMIN role to an existing one. Run again right before
     * {@link #persistInvitation} on the confirmed path too, since state may have drifted while the
     * confirmation email was pending.
     */
    private User validateInvitee(String email) {
        User invitee = userRepository.findByEmailWithRolesAndPermisos(email)
                .orElseThrow(AuthException::invitationRequiresRegisteredUser);

        if (!Boolean.TRUE.equals(invitee.getIsVerified())) {
            throw AuthException.invitationTargetNotVerified();
        }
        if (!Boolean.TRUE.equals(invitee.getIsActive())) {
            throw AuthException.accountDisabled();
        }
        if (invitee.getRoles().stream().anyMatch(r -> ADMIN_ROLE_NAME.equals(r.getName()))) {
            throw AuthException.userAlreadyAdmin();
        }
        adminInvitationRepository.findByInvitedEmailAndStatus(email, InvitationStatus.PENDING)
                .ifPresent(existing -> {
                    throw AuthException.invitationAlreadyPending();
                });
        return invitee;
    }

    private AdminInvitationResponse persistInvitation(User creator, User invitee) {
        String rawToken = UUID.randomUUID().toString();

        AdminInvitation invitation = AdminInvitation.builder()
                .tokenHash(TokenHasher.hashToken(rawToken))
                .invitedEmail(invitee.getEmail())
                .roleToGrant(ADMIN_ROLE_NAME)
                .createdBy(creator)
                .expiresAt(LocalDateTime.now().plusHours(24))
                .status(InvitationStatus.PENDING)
                .build();

        invitation = adminInvitationRepository.save(invitation);
        emailService.sendAdminInvitationEmail(invitee.getEmail(), rawToken);
        auditService.record(creator, "ADMIN_INVITATION_CREATED", "admin_invitations", invitation.getId(), null, null, null);
        eventPublisher.publishEvent(new AdminInvitationCreatedEvent(
                invitation.getId(), invitee.getId(), invitation.getInvitedEmail(), creator.getId()));

        return toResponse(invitation);
    }

    @Override
    public List<AdminInvitationResponse> listInvitations() {
        return adminInvitationRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    public void validateInvitation(String token) {
        AdminInvitation invitation = adminInvitationRepository.findByTokenHash(TokenHasher.hashToken(token))
                .orElseThrow(AuthException::invalidInvitation);

        if (invitation.getStatus() != InvitationStatus.PENDING || invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw AuthException.invalidInvitation();
        }
    }

    @Override
    @Transactional
    public MessageResponse acceptInvitation(AcceptAdminInvitationRequest request) {
        AdminInvitation invitation = loadPendingInvitationByToken(request.token());
        User user = currentUserProvider.getCurrentUser();

        if (!user.getEmail().equalsIgnoreCase(invitation.getInvitedEmail())) {
            throw AuthException.invitationEmailMismatch();
        }

        Role adminRole = roleRepository.findByName(ADMIN_ROLE_NAME)
                .orElseThrow(() -> new IllegalStateException("ADMIN role is missing"));

        user.getRoles().add(adminRole);
        userRepository.save(user);

        invitation.setStatus(InvitationStatus.ACCEPTED);
        invitation.setResolvedAt(LocalDateTime.now());
        invitation.setAcceptedBy(user);
        adminInvitationRepository.save(invitation);

        auditService.record(user, "ADMIN_INVITATION_ACCEPTED", "admin_invitations", invitation.getId(), null, null, null);
        eventPublisher.publishEvent(new AdminInvitationAcceptedEvent(
                invitation.getId(), user.getId(), user.getEmail(), invitation.getCreatedBy().getId()));

        return new MessageResponse("Invitación aceptada. Ahora tienes permisos de administrador.");
    }

    @Override
    @Transactional
    public MessageResponse rejectInvitation(RejectAdminInvitationRequest request) {
        AdminInvitation invitation = loadPendingInvitationByToken(request.token());

        invitation.setStatus(InvitationStatus.REJECTED);
        invitation.setResolvedAt(LocalDateTime.now());
        adminInvitationRepository.save(invitation);

        auditService.record(null, "ADMIN_INVITATION_REJECTED", "admin_invitations", invitation.getId(), null, null, null);
        eventPublisher.publishEvent(new AdminInvitationRejectedEvent(
                invitation.getId(), invitation.getInvitedEmail(), invitation.getCreatedBy().getId()));

        return new MessageResponse("Invitación rechazada.");
    }

    @Override
    @Transactional
    public AdminInvitationResponse resendInvitation(Long invitationId) {
        User admin = currentUserProvider.getCurrentUser();
        AdminInvitation invitation = adminInvitationRepository.findById(invitationId)
                .orElseThrow(AuthException::invalidInvitation);

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw AuthException.invitationNotPending();
        }

        String rawToken = UUID.randomUUID().toString();
        invitation.setTokenHash(TokenHasher.hashToken(rawToken));
        invitation.setExpiresAt(LocalDateTime.now().plusHours(24));
        invitation = adminInvitationRepository.save(invitation);

        emailService.sendAdminInvitationEmail(invitation.getInvitedEmail(), rawToken);
        auditService.record(admin, "ADMIN_INVITATION_RESENT", "admin_invitations", invitation.getId(), null, null, null);

        return toResponse(invitation);
    }

    @Override
    @Transactional
    public AdminInvitationResponse cancelInvitation(Long invitationId) {
        User admin = currentUserProvider.getCurrentUser();
        AdminInvitation invitation = adminInvitationRepository.findById(invitationId)
                .orElseThrow(AuthException::invalidInvitation);

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw AuthException.invitationNotPending();
        }

        invitation.setStatus(InvitationStatus.CANCELLED);
        invitation.setResolvedAt(LocalDateTime.now());
        invitation = adminInvitationRepository.save(invitation);

        auditService.record(admin, "ADMIN_INVITATION_CANCELLED", "admin_invitations", invitation.getId(), null, null, null);

        return toResponse(invitation);
    }

    private AdminInvitation loadPendingInvitationByToken(String token) {
        AdminInvitation invitation = adminInvitationRepository.findByTokenHash(TokenHasher.hashToken(token))
                .orElseThrow(AuthException::invalidInvitation);

        if (invitation.getStatus() != InvitationStatus.PENDING || invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw AuthException.invalidInvitation();
        }
        return invitation;
    }

    private AdminInvitationResponse toResponse(AdminInvitation invitation) {
        InvitationStatus effectiveStatus = invitation.getStatus() == InvitationStatus.PENDING
                && invitation.getExpiresAt().isBefore(LocalDateTime.now())
                ? InvitationStatus.EXPIRED
                : invitation.getStatus();

        return new AdminInvitationResponse(
                invitation.getId(),
                invitation.getInvitedEmail(),
                invitation.getRoleToGrant(),
                invitation.getCreatedAt(),
                invitation.getExpiresAt(),
                effectiveStatus,
                invitation.getResolvedAt()
        );
    }

}
