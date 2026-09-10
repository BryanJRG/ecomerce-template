package com.bjdev.ecomercebase.dto.response;

import java.math.BigDecimal;

public record CartLineResponse(
        Long id,
        Long variantId,
        String itemName,
        String variantName,
        Integer quantity,
        BigDecimal priceAtAddition
) {
}
