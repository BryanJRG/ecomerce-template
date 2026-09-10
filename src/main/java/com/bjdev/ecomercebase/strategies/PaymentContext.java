package com.bjdev.ecomercebase.strategies;

import com.bjdev.ecomercebase.models.enums.PaymentType;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Deliberately carries no Order: payment is processed BEFORE the Order exists (see
 * CheckoutServiceImpl's reservation-first ordering), so strategies only get what they need to
 * charge — the cart/client this attempt belongs to, the amount, and an opaque provider payload.
 */
public record PaymentContext(
        Long clientId,
        Long cartId,
        BigDecimal amount,
        PaymentType paymentType,
        String idempotencyKey,
        Map<String, String> paymentData
) {
}
