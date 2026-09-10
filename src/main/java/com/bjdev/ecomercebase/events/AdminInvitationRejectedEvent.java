package com.bjdev.ecomercebase.events;

public record AdminInvitationRejectedEvent(
        Long invitationId,
        String invitedEmail,
        Long createdByUserId
) {
}
