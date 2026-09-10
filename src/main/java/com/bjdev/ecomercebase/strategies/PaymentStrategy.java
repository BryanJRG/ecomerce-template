package com.bjdev.ecomercebase.strategies;

import com.bjdev.ecomercebase.models.enums.PaymentType;

/** One implementation per payment method (CARD, GOOGLE_PAY) — see PaymentStrategyFactory. */
public interface PaymentStrategy {

    PaymentResult process(PaymentContext context);

    PaymentType supports();
}
