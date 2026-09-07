package com.bjdev.base.dto.request;

import jakarta.validation.constraints.NotBlank;

/** The invited user must already exist and be authenticated; this just confirms the token. */
public record AcceptAdminInvitationRequest(@NotBlank String token) {
}
