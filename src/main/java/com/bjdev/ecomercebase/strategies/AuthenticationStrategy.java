package com.bjdev.ecomercebase.strategies;

import com.bjdev.ecomercebase.dto.request.LoginRequest;
import com.bjdev.ecomercebase.models.auth.AuthProvider;
import com.bjdev.ecomercebase.models.user.User;

/** One implementation per login provider (LOCAL, GOOGLE, and future ones like GITHUB). */
public interface AuthenticationStrategy {

    AuthProvider getProvider();

    User login(LoginRequest request, String clientIp);
}
