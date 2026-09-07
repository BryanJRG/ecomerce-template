package com.bjdev.base.strategies;

import com.bjdev.base.dto.request.RegisterRequest;
import com.bjdev.base.models.auth.AuthProvider;
import com.bjdev.base.models.user.User;

/** One implementation per self-registration provider. OAuth providers auto-provision on login instead. */
public interface RegistrationStrategy {

    AuthProvider getProvider();

    User register(RegisterRequest request);
}
