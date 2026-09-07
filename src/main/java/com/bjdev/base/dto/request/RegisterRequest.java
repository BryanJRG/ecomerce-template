package com.bjdev.base.dto.request;

import com.bjdev.base.models.auth.AuthProvider;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotNull AuthProvider provider,
        @NotBlank @Email String email,
        @Size(min = 8, max = 100) String password,
        @NotBlank @Size(max = 45) String userName,
        @NotBlank @Size(max = 100) String firstName,
        @NotBlank @Size(max = 100) String lastName
) {
}
