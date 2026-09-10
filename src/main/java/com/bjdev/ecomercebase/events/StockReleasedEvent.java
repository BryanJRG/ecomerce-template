package com.bjdev.ecomercebase.events;

/**
 * Compensation event: published when a checkout fails after stock was already reserved (payment
 * declined, or the orphaned-reservation cleanup job timing one out). The listener performs the
 * actual atomic release — the publisher never touches stock directly, so every release path goes
 * through the same code.
 */
public record StockReleasedEvent(Long stockReservationId, Long variantId, Integer quantity) {
}
