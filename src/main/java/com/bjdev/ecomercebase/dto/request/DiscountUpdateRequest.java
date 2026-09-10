package com.bjdev.ecomercebase.dto.request;

import com.bjdev.ecomercebase.models.enums.DiscountType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * All fields nullable by design: DiscountServiceImpl (a later task) applies only the non-null
 * scalar ones via MapStruct (NullValuePropertyMappingStrategy.IGNORE), same pattern as
 * ItemUpdateRequest. categoryIds/itemIds/variantIds/excludedVariantIds are resolved by the service
 * (diffed against the existing targeting/exclusion tables), not mapped directly onto the entity.
 */
public record DiscountUpdateRequest(
        String title,
        DiscountType type,
        BigDecimal value,
        LocalDateTime startDate,
        LocalDateTime endDate,
        Boolean active,
        String code,
        BigDecimal minPurchaseAmount,
        Integer maxUsesTotal,
        Integer maxUsesPerClient,
        Boolean applyToAllCatalog,
        List<Long> categoryIds,
        List<Long> itemIds,
        List<Long> variantIds,
        List<Long> excludedVariantIds
) {
}
