package com.bjdev.ecomercebase.dto.request;

import java.util.List;

/**
 * All fields nullable/optional by design: ItemServiceImpl applies only the non-null ones via
 * MapStruct (NullValuePropertyMappingStrategy.IGNORE) instead of manual if/else checks.
 * category/brand/variantPriceUpdates are resolved by the service (they need repository lookups
 * and re-validation), not mapped directly onto the entity.
 */
public record ItemUpdateRequest(
        String name,
        String description,
        Long categoryId,
        Long brandId,
        List<ItemVariantPriceUpdate> variantPriceUpdates
) {
}
