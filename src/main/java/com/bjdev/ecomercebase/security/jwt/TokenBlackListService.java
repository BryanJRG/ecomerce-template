package com.bjdev.ecomercebase.security.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class TokenBlackListService {

    private final StringRedisTemplate redisTemplate;

    private static final String BLACKLIST_PREFIX= "jwt:blacklist:";
    private static final String INVALIDATE_BEFORE_PREFIX = "jwt:invalidate-before:";

    /** Longest possible lifetime of any refresh token issued; used as the TTL ceiling for the invalidation marker. */
    private static final Duration MAX_TOKEN_LIFETIME = Duration.ofDays(14);

    public void blacklistToken(String jti, long ttlSeconds) {
        if (jti != null && ttlSeconds > 0) {
            redisTemplate.opsForValue().set(
                    BLACKLIST_PREFIX + jti,
                    "revoked",
                    Duration.ofSeconds(ttlSeconds)
            );
        }
    }

    public boolean isTokenBlacklisted(String jti) {
        if (jti == null) return false;
        return Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_PREFIX + jti));
    }

    /**
     * Invalidates every token issued for this user up to now (account disable, password change/reset,
     * login when single-session is enabled). Tokens issued after this call remain valid.
     */
    public void invalidateAllUserTokens(String username) {
        redisTemplate.opsForValue().set(
                INVALIDATE_BEFORE_PREFIX + username,
                String.valueOf(markerMillis()),
                MAX_TOKEN_LIFETIME
        );
    }

    public boolean isTokenValidForUser(String username, long issuedAtMillis) {
        String value = redisTemplate.opsForValue().get(INVALIDATE_BEFORE_PREFIX + username);
        if (value == null) return true;
        // >= on purpose: a token minted at-or-after the marker is exactly what should stay valid after
        // an invalidate-then-reissue call (login/changePassword/resetPassword all reissue tokens right
        // after invalidating); only tokens from strictly before the marker must be rejected.
        return issuedAtMillis >= Long.parseLong(value);
    }

    /**
     * JWT "iat" is second-precision (RFC 7519 NumericDate — {@link JwtTokenProvider#getIssuedAtMillis}
     * returns it as a whole-second millisecond value), while {@code System.currentTimeMillis()} carries
     * sub-second precision. Comparing those directly made the marker almost always "later" than a token
     * minted in the very same second right after it was set — e.g. marker=...364554 vs iat=...364000 —
     * so every invalidate-then-reissue call (every single-session login, every password change/reset)
     * immediately invalidated the token it had just issued. Truncating the marker to whole seconds keeps
     * both sides at the same precision so a same-second reissue compares equal (kept via the >= above)
     * while anything from an earlier second still compares less (correctly invalidated).
     */
    private long markerMillis() {
        return (System.currentTimeMillis() / 1000) * 1000;
    }
}


