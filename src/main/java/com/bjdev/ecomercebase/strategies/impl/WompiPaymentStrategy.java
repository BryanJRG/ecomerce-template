package com.bjdev.ecomercebase.strategies.impl;

import com.bjdev.ecomercebase.models.enums.PaymentType;
import com.bjdev.ecomercebase.strategies.PaymentContext;
import com.bjdev.ecomercebase.strategies.PaymentResult;
import com.bjdev.ecomercebase.strategies.PaymentStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

/**
 * Structures and "sends" the 3DS card-charge request to Wompi. Scope note: a real integration
 * sends the card data + returns a redirect for the 3DS challenge, then only learns the final
 * outcome later via an async webhook (see StockReservation's class doc for how CheckoutService
 * stays safe across that gap). Implementing that webhook is explicitly a separate task — this
 * strategy simulates a synchronous provider response instead, so {@link #process} always returns
 * a final approved/declined result.
 */
@Slf4j
@Component
public class WompiPaymentStrategy implements PaymentStrategy {

    @Override
    public PaymentResult process(PaymentContext context) {
        Map<String, String> cardData = context.paymentData();
        if (cardData == null || cardData.get("cardToken") == null) {
            return PaymentResult.declined("Falta el token de tarjeta.");
        }

        log.info("Submitting Wompi 3DS charge: cart={}, amount={}, idempotencyKey={}",
                context.cartId(), context.amount(), context.idempotencyKey());

        return simulateWompiResponse(cardData);
    }

    /** Stand-in for the real Wompi API + 3DS challenge round-trip. */
    private PaymentResult simulateWompiResponse(Map<String, String> cardData) {
        if ("true".equalsIgnoreCase(cardData.get("simulateDecline"))) {
            return PaymentResult.declined("Tarjeta rechazada por el emisor.");
        }
        return PaymentResult.approved("WOMPI-" + UUID.randomUUID());
    }

    @Override
    public PaymentType supports() {
        return PaymentType.CARD;
    }
}
