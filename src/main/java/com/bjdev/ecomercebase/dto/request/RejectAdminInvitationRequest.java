package com.bjdev.ecomercebase.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RejectAdminInvitationRequest(@NotBlank String token) {
}
