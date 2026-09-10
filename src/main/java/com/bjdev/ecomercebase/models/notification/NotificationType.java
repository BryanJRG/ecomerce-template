package com.bjdev.ecomercebase.models.notification;

/**
 * Default, built-in notification type keys. This is intentionally a plain constants holder
 * (not an enum): {@link Notification#getType()} is a free-form namespaced String so future
 * modules/variants can define their own keys (e.g. "billing.invoice_overdue") without ever
 * touching this class.
 */
public final class NotificationType {

    private NotificationType() {
    }

    public static final String ADMIN_INVITATION_CREATED = "admin_invitation.created";
    public static final String ADMIN_INVITATION_ACCEPTED = "admin_invitation.accepted";
    public static final String ADMIN_INVITATION_REJECTED = "admin_invitation.rejected";
}
