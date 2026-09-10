package com.bjdev.ecomercebase.strategies.impl;

import com.bjdev.ecomercebase.models.enums.PaymentType;
import com.bjdev.ecomercebase.strategies.PaymentContext;
import com.bjdev.ecomercebase.strategies.PaymentResult;
import com.bjdev.ecomercebase.strategies.PaymentStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

/** Same contract as WompiPaymentStrategy, delegating to the Google Pay API instead — see its class doc for scope notes. */
@Slf4j
@Component
public class GooglePayPaymentStrategy implements PaymentStrategy {

    @Override
    public PaymentResult process(PaymentContext context) {
        Map<String, String> payload = context.paymentData();
        if (payload == null || payload.get("googlePayToken") == null) {
            return PaymentResult.declined("Falta el token de Google Pay.");
        }

        log.info("Submitting Google Pay charge: cart={}, amount={}, idempotencyKey={}",
                context.cartId(), context.amount(), context.idempotencyKey());

        return simulateGooglePayResponse(payload);
    }

    private PaymentResult simulateGooglePayResponse(Map<String, String> payload) {
        if ("true".equalsIgnoreCase(payload.get("simulateDecline"))) {
            return PaymentResult.declined("Pago rechazado por Google Pay.");
        }
        return PaymentResult.approved("GPAY-" + UUID.randomUUID());
    }

    @Override
    public PaymentType supports() {
        return PaymentType.GOOGLE_PAY;
    }
}
