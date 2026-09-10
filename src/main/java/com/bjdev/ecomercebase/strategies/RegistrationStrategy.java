package com.bjdev.ecomercebase.strategies;

import com.bjdev.ecomercebase.dto.request.RegisterRequest;
import com.bjdev.ecomercebase.models.auth.AuthProvider;
import com.bjdev.ecomercebase.models.user.User;

/** One implementation per self-registration provider. OAuth providers auto-provision on login instead. */
public interface RegistrationStrategy {

    AuthProvider getProvider();

    User register(RegisterRequest request);
}
