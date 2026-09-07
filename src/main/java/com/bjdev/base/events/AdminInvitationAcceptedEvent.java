package com.bjdev.base.events;

public record AdminInvitationAcceptedEvent(
        Long invitationId,
        Long acceptedByUserId,
        String acceptedByEmail,
        Long createdByUserId
) {
}
