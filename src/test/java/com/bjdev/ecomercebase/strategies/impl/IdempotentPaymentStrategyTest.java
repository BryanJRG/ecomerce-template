package com.bjdev.ecomercebase.strategies.impl;

import com.bjdev.ecomercebase.exception.CheckoutException;
import com.bjdev.ecomercebase.models.enums.PaymentType;
import com.bjdev.ecomercebase.strategies.PaymentContext;
import com.bjdev.ecomercebase.strategies.PaymentResult;
import com.bjdev.ecomercebase.strategies.PaymentStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Covers the safety property this decorator exists for: a retried checkout with the same
 * idempotency key must never charge twice, and a key reused with a DIFFERENT request must never
 * silently return someone else's cached approval.
 */
@ExtendWith(MockitoExtension.class)
class IdempotentPaymentStrategyTest {

    private static final String KEY = "payment:idem:idem-key-1";
    private static final PaymentContext CONTEXT = new PaymentContext(
            1L, 2L, new BigDecimal("100.00"), PaymentType.CARD, "idem-key-1", Map.of());

    @Mock
    private PaymentStrategy delegate;
    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private IdempotentPaymentStrategy strategy;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        strategy = new IdempotentPaymentStrategy(delegate, redisTemplate, objectMapper, Duration.ofMinutes(10));
    }

    @Test
    void process_cacheMiss_callsDelegateAndCachesResult() {
        when(valueOperations.get(KEY)).thenReturn(null);
        PaymentResult approved = PaymentResult.approved("txn-1");
        when(delegate.process(CONTEXT)).thenReturn(approved);

        PaymentResult result = strategy.process(CONTEXT);

        assertThat(result).isEqualTo(approved);
        verify(delegate).process(CONTEXT);
        verify(valueOperations).set(eq(KEY), any(), eq(Duration.ofMinutes(10)));
    }

    @Test
    void process_retryWithSameKeyAndSameFingerprint_returnsCachedResultWithoutChargingAgain() {
        when(valueOperations.get(KEY)).thenReturn(null);
        when(delegate.process(CONTEXT)).thenReturn(PaymentResult.approved("txn-1"));
        strategy.process(CONTEXT);

        ArgumentCaptor<String> cachedJson = ArgumentCaptor.forClass(String.class);
        verify(valueOperations).set(eq(KEY), cachedJson.capture(), any(Duration.class));
        when(valueOperations.get(KEY)).thenReturn(cachedJson.getValue());

        PaymentResult retry = strategy.process(CONTEXT);

        assertThat(retry.providerTransactionId()).isEqualTo("txn-1");
        verify(delegate, times(1)).process(any()); // still only the original call
    }

    @Test
    void process_sameKeyWithDifferentAmount_throwsInsteadOfTrustingTheCache() {
        when(valueOperations.get(KEY)).thenReturn(null);
        when(delegate.process(CONTEXT)).thenReturn(PaymentResult.approved("txn-1"));
        strategy.process(CONTEXT);

        ArgumentCaptor<String> cachedJson = ArgumentCaptor.forClass(String.class);
        verify(valueOperations).set(eq(KEY), cachedJson.capture(), any(Duration.class));
        when(valueOperations.get(KEY)).thenReturn(cachedJson.getValue());

        PaymentContext replayedWithHigherAmount = new PaymentContext(
                CONTEXT.clientId(), CONTEXT.cartId(), new BigDecimal("999.00"),
                CONTEXT.paymentType(), CONTEXT.idempotencyKey(), CONTEXT.paymentData());

        assertThatThrownBy(() -> strategy.process(replayedWithHigherAmount))
                .isInstanceOfSatisfying(CheckoutException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo("IDEMPOTENCY_KEY_REUSED"));
        verify(delegate, times(1)).process(any()); // never charged for the mismatched replay
    }
}
