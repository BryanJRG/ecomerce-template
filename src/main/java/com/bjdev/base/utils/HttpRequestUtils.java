package com.bjdev.base.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

public final class HttpRequestUtils {

    private HttpRequestUtils() {
    }

    /**
     * Resolves the real client IP, accounting for requests proxied through Cloudflare.
     *
     * <p>Trusts {@code CF-Connecting-IP}/{@code X-Forwarded-For} unconditionally, which is only safe
     * because the deployment's threat model assumes the origin is <b>not</b> directly reachable from
     * the internet — every request, on every route (not just {@code /api/admin/**}, which is the only
     * path {@link com.bjdev.base.config.CloudflareTunnelFilter} additionally gates with a shared
     * secret), is expected to arrive through Cloudflare. If that assumption ever stops holding for a
     * given deployment (origin reachable directly, another reverse proxy in front, etc.), a client
     * could spoof these headers to falsify {@code AuditLog} entries and evade the per-IP brute-force
     * counter in {@code LoginAttemptService} — firewall the origin accordingly, or stop trusting these
     * headers outside of routes actually behind the tunnel filter.
     */
    public static String getClientIp(HttpServletRequest request) {
        String cfIp = request.getHeader("CF-Connecting-IP");
        if (StringUtils.hasText(cfIp)) {
            return cfIp;
        }

        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwardedFor)) {
            return forwardedFor.split(",")[0].trim();
        }

        return request.getRemoteAddr();
    }

    public static String getUserAgent(HttpServletRequest request) {
        return request.getHeader("User-Agent");
    }
}
