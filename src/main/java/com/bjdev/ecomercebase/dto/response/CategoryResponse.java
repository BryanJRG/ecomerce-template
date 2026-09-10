package com.bjdev.ecomercebase.dto.response;

import java.math.BigDecimal;

public record CategoryResponse(
        Long id,
        String name,
        String slug,
        Boolean active,
        BigDecimal minPrice,
        BigDecimal maxPrice
) {
}
