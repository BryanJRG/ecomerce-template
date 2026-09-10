package com.bjdev.ecomercebase.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ItemVariantPriceUpdate(
        @NotNull Long variantId,
        @NotNull @DecimalMin(value = "0.0", inclusive = true) BigDecimal price
) {
}
