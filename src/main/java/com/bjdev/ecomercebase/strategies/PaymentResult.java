package com.bjdev.ecomercebase.strategies;

public record PaymentResult(boolean approved, String providerTransactionId, String failureReason) {

    public static PaymentResult approved(String providerTransactionId) {
        return new PaymentResult(true, providerTransactionId, null);
    }

    public static PaymentResult declined(String reason) {
        return new PaymentResult(false, null, reason);
    }
}
