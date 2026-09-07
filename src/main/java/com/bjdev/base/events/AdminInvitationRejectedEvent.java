package com.bjdev.base.events;

public record AdminInvitationRejectedEvent(
        Long invitationId,
        String invitedEmail,
        Long createdByUserId
) {
}
