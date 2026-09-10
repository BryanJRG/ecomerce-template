package com.bjdev.ecomercebase.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Binds {@code app.cors.*} from application-*.yml so CORS origins/methods/headers can be changed
 * per environment (e.g. via an env var in prod) without touching SecurityConfig or recompiling.
 */
@Component
@ConfigurationProperties(prefix = "app.cors")
@Getter
@Setter
public class CorsProperties {

    private List<String> allowedOriginPatterns = List.of("http://localhost:*");
    private List<String> allowedMethods = List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS");
    private List<String> allowedHeaders = List.of("*");
    private List<String> exposedHeaders = List.of("Authorization", "Content-Type");
    private boolean allowCredentials = true;
    private long maxAge = 3600L;
}
