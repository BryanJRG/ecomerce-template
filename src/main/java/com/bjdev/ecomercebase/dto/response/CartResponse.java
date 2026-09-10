package com.bjdev.ecomercebase.dto.response;

import com.bjdev.ecomercebase.models.enums.CartStatus;

import java.util.List;

public record CartResponse(
        Long id,
        Long clientId,
        CartStatus status,
        List<CartLineResponse> lines
) {
}
