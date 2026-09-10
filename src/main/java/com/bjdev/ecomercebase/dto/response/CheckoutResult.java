package com.bjdev.ecomercebase.dto.response;

import com.bjdev.ecomercebase.models.enums.OrderStatus;
import com.bjdev.ecomercebase.models.enums.PaymentStatus;

import java.math.BigDecimal;

public record CheckoutResult(
        Long orderId,
        OrderStatus orderStatus,
        PaymentStatus paymentStatus,
        BigDecimal total,
        String providerTransactionId,
        /** Null/zero when no discount applied — total above is already net of this. */
        BigDecimal discountAmount
) {
}
