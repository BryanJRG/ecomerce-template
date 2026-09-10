package com.bjdev.ecomercebase.dto.response;

public record ShippingAddressResponse(
        Long id,
        String label,
        String recipientName,
        String phone,
        String addressLine1,
        String addressLine2,
        String city,
        String department,
        Boolean isDefault
) {
}
