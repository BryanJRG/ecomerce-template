package com.bjdev.base.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RejectAdminInvitationRequest(@NotBlank String token) {
}
