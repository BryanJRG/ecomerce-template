package com.bjdev.base.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateAdminInvitationRequest(@NotBlank @Email String email) {
}
