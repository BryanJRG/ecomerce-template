package com.bjdev.ecomercebase.events;

/** Published after an admin invitation is successfully committed. Carries ids only — the
 *  listener re-fetches whatever entities it needs, since it runs after the original transaction
 *  (and its persistence context) is gone. */
public record AdminInvitationCreatedEvent(
        Long invitationId,
        Long invitedUserId,
        String invitedEmail,
        Long createdByUserId
) {
}
