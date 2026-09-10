package com.bjdev.ecomercebase.dto.request;

import com.bjdev.ecomercebase.models.enums.PaymentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

/**
 * {@code paymentData} is an opaque, provider-specific payload (card token, Google Pay token,
 * etc.) — kept generic on purpose so adding a provider never requires changing this DTO.
 * {@code idempotencyKey} is client-generated (e.g. a UUID minted once per checkout attempt in
 * the frontend) and is what IdempotentPaymentStrategy keys its Redis cache on.
 */
public record CheckoutRequest(
        @NotNull Long cartId,
        Long shippingAddressId,
        @NotNull PaymentType paymentType,
        @NotBlank String idempotencyKey,
        Map<String, String> paymentData,
        /** Null means "no code" — an eligible auto-apply discount (if any) is still applied. */
        String discountCode
) {
}
