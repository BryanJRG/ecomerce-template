package com.bjdev.ecomercebase.strategies.impl;

import com.bjdev.ecomercebase.exception.CheckoutException;
import com.bjdev.ecomercebase.models.enums.PaymentType;
import com.bjdev.ecomercebase.strategies.PaymentContext;
import com.bjdev.ecomercebase.strategies.PaymentResult;
import com.bjdev.ecomercebase.strategies.PaymentStrategy;
import tools.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.Objects;

/**
 * Decorator (not a Spring bean — built and owned by PaymentStrategyFactory) that makes a
 * network-facing strategy safe to retry: a client that times out waiting for a response and
 * resubmits with the same idempotency key gets the cached outcome instead of being charged
 * twice.
 *
 * The cached entry also stores a fingerprint of the request (amount/cart/client/type). If the
 * same key ever comes back with a different fingerprint, that's not a legitimate retry — it's
 * either a client bug or an attempt to replay an old approval against a new, larger charge — so
 * this refuses to serve the cached result and raises a conflict instead of silently trusting it.
 */
@Slf4j
public class IdempotentPaymentStrategy implements PaymentStrategy {

    private static final String KEY_PREFIX = "payment:idem:";

    private final PaymentStrategy delegate;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final Duration ttl;

    public IdempotentPaymentStrategy(PaymentStrategy delegate, StringRedisTemplate redisTemplate,
                                      ObjectMapper objectMapper, Duration ttl) {
        this.delegate = delegate;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.ttl = ttl;
    }

    private record CachedPayment(String fingerprint, PaymentResult result) {
    }

    @Override
    public PaymentResult process(PaymentContext context) {
        String key = KEY_PREFIX + context.idempotencyKey();
        String fingerprint = fingerprintOf(context);

        CachedPayment cached = readCached(key);
        if (cached != null) {
            if (!Objects.equals(cached.fingerprint(), fingerprint)) {
                throw CheckoutException.idempotencyKeyReused();
            }
            log.info("Idempotency hit for key={}, returning cached payment result", context.idempotencyKey());
            return cached.result();
        }

        PaymentResult result = delegate.process(context);
        writeCached(key, new CachedPayment(fingerprint, result));
        return result;
    }

    private String fingerprintOf(PaymentContext context) {
        return context.clientId() + ":" + context.cartId() + ":" + context.paymentType() + ":" + context.amount();
    }

    private CachedPayment readCached(String key) {
        String raw = redisTemplate.opsForValue().get(key);
        if (raw == null) {
            return null;
        }
        try {
            return objectMapper.readValue(raw, CachedPayment.class);
        } catch (Exception e) {
            log.warn("Failed to deserialize cached payment result for key={}, treating as cache miss", key, e);
            return null;
        }
    }

    private void writeCached(String key, CachedPayment cached) {
        try {
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(cached), ttl);
        } catch (Exception e) {
            // Caching is a safety net, not the source of truth — a serialization failure must not fail the payment.
            log.warn("Failed to cache payment result for key={}", key, e);
        }
    }

    @Override
    public PaymentType supports() {
        return delegate.supports();
    }
}
