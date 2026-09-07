package com.bjdev.base.dto.request;

import com.bjdev.base.models.auth.AuthProvider;
import jakarta.validation.constraints.NotNull;

public record LoginRequest(
        @NotNull AuthProvider provider,
        String email,
        String password,
        String idToken
) {
}
