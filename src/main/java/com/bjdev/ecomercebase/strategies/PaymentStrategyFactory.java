package com.bjdev.ecomercebase.strategies;

import com.bjdev.ecomercebase.exception.CheckoutException;
import com.bjdev.ecomercebase.models.enums.PaymentType;
import com.bjdev.ecomercebase.strategies.impl.IdempotentPaymentStrategy;
import tools.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Resolves the right {@link PaymentStrategy} for a {@link PaymentType}, the same way
 * AuthStrategyResolver does for auth providers — adding a new payment method is just a new
 * strategy bean and a new enum entry.
 *
 * Every strategy is wrapped with {@link IdempotentPaymentStrategy} because both CARD and
 * GOOGLE_PAY go over the network to a provider a client might legitimately retry against.
 */
@Component
public class PaymentStrategyFactory {

    private final List<PaymentStrategy> paymentStrategies;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final Duration idempotencyTtl;

    private final Map<PaymentType, PaymentStrategy> strategies = new EnumMap<>(PaymentType.class);

    public PaymentStrategyFactory(List<PaymentStrategy> paymentStrategies,
                                   StringRedisTemplate redisTemplate,
                                   ObjectMapper objectMapper,
                                   @Value("${app.payment.idempotency-ttl-minutes:10}") long idempotencyTtlMinutes) {
        this.paymentStrategies = paymentStrategies;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.idempotencyTtl = Duration.ofMinutes(idempotencyTtlMinutes);
    }

    @PostConstruct
    void init() {
        for (PaymentStrategy strategy : paymentStrategies) {
            strategies.put(strategy.supports(),
                    new IdempotentPaymentStrategy(strategy, redisTemplate, objectMapper, idempotencyTtl));
        }
    }

    public PaymentStrategy resolve(PaymentType type) {
        PaymentStrategy strategy = strategies.get(type);
        if (strategy == null) {
            throw CheckoutException.unsupportedPaymentType();
        }
        return strategy;
    }
}
