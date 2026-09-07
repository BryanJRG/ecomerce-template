package com.bjdev.base.dto.response;

import com.bjdev.base.models.auth.InvitationStatus;

import java.time.LocalDateTime;

public record AdminInvitationResponse(
        Long id,
        String invitedEmail,
        String roleToGrant,
        LocalDateTime createdAt,
        LocalDateTime expiresAt,
        InvitationStatus status,
        LocalDateTime resolvedAt
) {
}
