package com.bjdev.ecomercebase.dto.request;

/** All fields nullable/optional by design — only the non-null ones are applied, same pattern as CategoryUpdateRequest. */
public record ShippingAddressUpdateRequest(
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
