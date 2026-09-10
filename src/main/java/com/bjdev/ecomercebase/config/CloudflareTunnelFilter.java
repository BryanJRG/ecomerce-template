package com.bjdev.ecomercebase.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Guards {@code /api/admin/**} with a shared secret header that only Cloudflare (via a Worker
 * or Transform Rule in front of the Tunnel) is expected to attach. Disabled in dev via
 * {@code app.cloudflare.tunnel-filter.enabled=false}; enable it in prod once the Tunnel is set up.
 */
@Slf4j
@Component
public class CloudflareTunnelFilter extends OncePerRequestFilter {

    private static final String ADMIN_PATH_PATTERN = "/api/admin/**";
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Value("${app.cloudflare.tunnel-filter.enabled:false}")
    private boolean enabled;

    @Value("${app.cloudflare.tunnel-filter.header-name:X-Tunnel-Secret}")
    private String headerName;

    @Value("${app.cloudflare.tunnel-filter.secret:}")
    private String secret;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        boolean isAdminPath = pathMatcher.match(ADMIN_PATH_PATTERN, request.getRequestURI());

        if (isAdminPath && enabled) {
            String header = request.getHeader(headerName);
            if (header == null || !constantTimeEquals(header, secret)) {
                log.warn("Blocked admin request without a valid tunnel secret from {}", request.getRemoteAddr());
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    /** Avoids leaking the secret one byte at a time via response-time differences (String.equals short-circuits). */
    private boolean constantTimeEquals(String header, String secret) {
        return MessageDigest.isEqual(header.getBytes(StandardCharsets.UTF_8), secret.getBytes(StandardCharsets.UTF_8));
    }
}
