package com.bjdev.base.models.auth;

public enum InvitationStatus {
    PENDING,
    ACCEPTED,
    REJECTED,
    CANCELLED,
    /** Never persisted — computed for display when a PENDING invitation's expiresAt has passed. */
    EXPIRED
}
