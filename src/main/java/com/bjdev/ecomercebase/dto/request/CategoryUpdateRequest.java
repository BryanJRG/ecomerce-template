package com.bjdev.ecomercebase.dto.request;

import java.math.BigDecimal;

/** All fields optional — only non-null ones are applied. */
public record CategoryUpdateRequest(
        String name,
        String slug,
        BigDecimal minPrice,
        BigDecimal maxPrice
) {
}
