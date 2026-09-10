package com.bjdev.ecomercebase.repositories.auth;

import com.bjdev.ecomercebase.models.auth.AdminInvitation;
import com.bjdev.ecomercebase.models.auth.InvitationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminInvitationRepository extends JpaRepository<AdminInvitation, Long> {
    Optional<AdminInvitation> findByTokenHash(String tokenHash);

    Optional<AdminInvitation> findByInvitedEmailAndStatus(String invitedEmail, InvitationStatus status);
}
