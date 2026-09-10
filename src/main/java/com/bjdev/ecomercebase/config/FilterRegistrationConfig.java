package com.bjdev.ecomercebase.config;

import com.bjdev.ecomercebase.security.jwt.JwtAuthenticationFilter;
import jakarta.servlet.Filter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * {@link JwtAuthenticationFilter} and {@link CloudflareTunnelFilter} are {@code @Component}
 * beans wired explicitly into the Spring Security chain (see {@code SecurityConfig}). Without
 * this, Spring Boot would ALSO auto-register them as generic servlet filters applied to every
 * request, running each of them twice per request.
 */
@Configuration
@RequiredArgsConstructor
public class FilterRegistrationConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CloudflareTunnelFilter cloudflareTunnelFilter;

    @Bean
    public FilterRegistrationBean<Filter> jwtAuthenticationFilterRegistration() {
        FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>(jwtAuthenticationFilter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<Filter> cloudflareTunnelFilterRegistration() {
        FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>(cloudflareTunnelFilter);
        registration.setEnabled(false);
        return registration;
    }
}
