package com.bjdev.base.repositories.auth;

import com.bjdev.base.models.auth.PendingAdminAction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PendingAdminActionRepository extends JpaRepository<PendingAdminAction, Long> {
    Optional<PendingAdminAction> findByTokenHash(String tokenHash);
}
