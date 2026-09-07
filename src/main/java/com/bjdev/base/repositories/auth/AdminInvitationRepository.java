package com.bjdev.base.repositories.auth;

import com.bjdev.base.models.auth.AdminInvitation;
import com.bjdev.base.models.auth.InvitationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminInvitationRepository extends JpaRepository<AdminInvitation, Long> {
    Optional<AdminInvitation> findByTokenHash(String tokenHash);

    Optional<AdminInvitation> findByInvitedEmailAndStatus(String invitedEmail, InvitationStatus status);
}
