package com.bjdev.ecomercebase.strategies;

import com.bjdev.ecomercebase.exception.AuthException;
import com.bjdev.ecomercebase.models.auth.AuthProvider;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Resolves the right {@link AuthenticationStrategy}/{@link RegistrationStrategy} for a given
 * {@link AuthProvider}. Adding a new provider (e.g. GitHub) is just a new strategy bean and a
 * new enum entry — no changes needed here or in the controllers.
 */
@Component
public class AuthStrategyResolver {

    private final List<AuthenticationStrategy> authenticationStrategies;
    private final List<RegistrationStrategy> registrationStrategies;

    private final Map<AuthProvider, AuthenticationStrategy> loginStrategies = new EnumMap<>(AuthProvider.class);
    private final Map<AuthProvider, RegistrationStrategy> registerStrategies = new EnumMap<>(AuthProvider.class);

    public AuthStrategyResolver(List<AuthenticationStrategy> authenticationStrategies,
                                 List<RegistrationStrategy> registrationStrategies) {
        this.authenticationStrategies = authenticationStrategies;
        this.registrationStrategies = registrationStrategies;
    }

    @PostConstruct
    void init() {
        authenticationStrategies.forEach(s -> loginStrategies.put(s.getProvider(), s));
        registrationStrategies.forEach(s -> registerStrategies.put(s.getProvider(), s));
    }

    public AuthenticationStrategy resolveLogin(AuthProvider provider) {
        AuthenticationStrategy strategy = loginStrategies.get(provider);
        if (strategy == null) {
            throw AuthException.invalidProvider();
        }
        return strategy;
    }

    public RegistrationStrategy resolveRegister(AuthProvider provider) {
        RegistrationStrategy strategy = registerStrategies.get(provider);
        if (strategy == null) {
            throw AuthException.invalidProvider();
        }
        return strategy;
    }
}
