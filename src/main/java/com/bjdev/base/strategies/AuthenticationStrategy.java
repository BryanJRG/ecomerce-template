package com.bjdev.base.strategies;

import com.bjdev.base.dto.request.LoginRequest;
import com.bjdev.base.models.auth.AuthProvider;
import com.bjdev.base.models.user.User;

/** One implementation per login provider (LOCAL, GOOGLE, and future ones like GITHUB). */
public interface AuthenticationStrategy {

    AuthProvider getProvider();

    User login(LoginRequest request, String clientIp);
}
