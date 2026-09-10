package com.bjdev.ecomercebase.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ShippingAddressCreateRequest(
        @NotBlank String label,
        @NotBlank String recipientName,
        @NotBlank String phone,
        @NotBlank String addressLine1,
        String addressLine2,
        @NotBlank String city,
        @NotBlank String department,
        boolean isDefault
) {
}
