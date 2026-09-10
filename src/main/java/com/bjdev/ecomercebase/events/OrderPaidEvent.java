package com.bjdev.ecomercebase.events;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Published after an order is confirmed as paid — see CheckoutOrderFinalizer. Carries values only
 * (no entity refs), same reasoning as ItemCreatedEvent: listeners re-fetch whatever state they need.
 */
public record OrderPaidEvent(Long orderId, BigDecimal total, LocalDateTime paidAt) {
}
