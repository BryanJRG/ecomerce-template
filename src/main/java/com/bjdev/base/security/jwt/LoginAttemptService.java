package com.bjdev.base.security.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Redis-backed brute-force throttle for login: separate counters per email (protects one
 * account from repeated guesses) and per IP (protects against credential stuffing across
 * many accounts from the same source).
 */
@Service
@RequiredArgsConstructor
public class LoginAttemptService {

    private final StringRedisTemplate redisTemplate;

    private static final String EMAIL_PREFIX = "login:fail:email:";
    private static final String IP_PREFIX = "login:fail:ip:";

    @Value("${app.login.max-attempts-per-email:5}")
    private int maxAttemptsPerEmail;

    @Value("${app.login.max-attempts-per-ip:20}")
    private int maxAttemptsPerIp;

    @Value("${app.login.lock-duration-minutes:15}")
    private long lockDurationMinutes;

    public void registerFailure(String email, String ip) {
        increment(EMAIL_PREFIX + email);
        increment(IP_PREFIX + ip);
    }

    public void registerSuccess(String email, String ip) {
        redisTemplate.delete(EMAIL_PREFIX + email);
        redisTemplate.delete(IP_PREFIX + ip);
    }

    public boolean isBlocked(String email, String ip) {
        return count(EMAIL_PREFIX + email) >= maxAttemptsPerEmail
                || count(IP_PREFIX + ip) >= maxAttemptsPerIp;
    }

    public long getLockDurationMinutes() {
        return lockDurationMinutes;
    }

    private void increment(String key) {
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1L) {
            redisTemplate.expire(key, Duration.ofMinutes(lockDurationMinutes));
        }
    }

    private long count(String key) {
        String value = redisTemplate.opsForValue().get(key);
        return value == null ? 0 : Long.parseLong(value);
    }
}
