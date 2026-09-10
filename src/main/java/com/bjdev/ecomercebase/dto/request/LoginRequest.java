package com.bjdev.ecomercebase.dto.request;

import com.bjdev.ecomercebase.models.auth.AuthProvider;
import jakarta.validation.constraints.NotNull;

public record LoginRequest(
        @NotNull AuthProvider provider,
        String email,
        String password,
        String idToken
) {
}
