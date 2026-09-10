package com.bjdev.ecomercebase.dto.response;

import java.math.BigDecimal;

public record ItemVariantResponse(
        Long id,
        String sku,
        String name,
        BigDecimal price,
        Integer stock,
        Boolean active
) {
}
