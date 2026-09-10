package com.bjdev.ecomercebase.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

/**
 * When {@code variants} is null/empty, ItemServiceImpl auto-creates a single "default" variant
 * from {@code sku}/{@code price}/{@code stock} below — for businesses without real variants
 * (e.g. a restaurant menu item). When {@code variants} is present, those top-level fields are
 * ignored in favor of the explicit list.
 */
public record ItemCreateRequest(
        @NotBlank String name,
        String description,
        @NotNull Long categoryId,
        Long brandId,
        @NotBlank String sku,
        @NotNull @DecimalMin(value = "0.0", inclusive = true) BigDecimal price,
        @NotNull @Min(0) Integer stock,
        Integer lowStockThreshold,
        List<ItemVariantRequest> variants
) {
}
