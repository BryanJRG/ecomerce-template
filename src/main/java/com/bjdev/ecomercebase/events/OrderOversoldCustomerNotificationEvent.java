package com.bjdev.ecomercebase.events;

/**
 * Residual-oversell case: the payment was already approved and the Order already persisted when
 * stock turned out to be unavailable (e.g. a manual stock edit in production raced the checkout).
 * Independent from {@link OrderOversoldAdminAlertEvent} on purpose — a failed customer email must
 * never suppress the admin alert, or vice versa.
 */
public record OrderOversoldCustomerNotificationEvent(Long orderId, Long clientId, String clientEmail) {
}
