package com.bjdev.base.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;

    private final UserDetailsService userDetailsService;

    private final TokenBlackListService tokenBlacklistService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String jwt = getJwtFromRequest(request);

        if(StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)){

            if(!"ACCESS".equals(tokenProvider.getTokenType(jwt))){
                log.warn("Tried to use a token different of ACCESS to authenticate");
                filterChain.doFilter(request, response);
                return;
            }

            String jti = tokenProvider.getJtiFromToken(jwt);
            if (jti == null || tokenBlacklistService.isTokenBlacklisted(jti)) {
                log.warn("Token with an invalid jti: {}", jti);
                filterChain.doFilter(request, response);
                return;
            }

            //Get username from token
            String userName = tokenProvider.getUsernameFromToken(jwt);

            long issuedAt = tokenProvider.getIssuedAtMillis(jwt);
            if (!tokenBlacklistService.isTokenValidForUser(userName, issuedAt)) {
                log.warn("Token issued before invalidation marker for user {}", userName);
                filterChain.doFilter(request, response);
                return;
            }

            //Load user details
            UserDetails userDetails = userDetailsService.loadUserByUsername(userName);

            //Create auth
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        // Get the token from the cookie
        if (request.getCookies() != null) {
            for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
                if ("accessToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        // If not possible, try from the Authorization header
        String bearerToken = request.getHeader("Authorization");

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // Remove "Bearer " prefix
        }

        // Deliberately no query-string fallback (e.g. "?token="): URLs end up in server/proxy access
        // logs, browser history, and the Referer header sent to any third-party resource on the page —
        // none of that applies to a cookie or an Authorization header. If a future SSE/EventSource
        // endpoint needs auth outside of cookies, give it its own short-lived, single-use ticket
        // scoped to that endpoint rather than accepting the full-privilege access token this way.
        return null;
    }
}
