package com.bjdev.ecomercebase.events;

public record AdminInvitationAcceptedEvent(
        Long invitationId,
        Long acceptedByUserId,
        String acceptedByEmail,
        Long createdByUserId
) {
}
