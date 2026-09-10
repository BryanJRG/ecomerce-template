package com.bjdev.ecomercebase.dto.response;

import com.bjdev.ecomercebase.models.enums.DiscountTargetType;
import com.bjdev.ecomercebase.models.enums.DiscountType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record DiscountResponse(
        Long id,
        String title,
        DiscountType type,
        BigDecimal value,
        DiscountTargetType targetType,
        LocalDateTime startDate,
        LocalDateTime endDate,
        Boolean active,
        String code,
        BigDecimal minPurchaseAmount,
        Integer maxUsesTotal,
        Integer maxUsesPerClient,
        Integer usedCount,
        List<Long> categoryIds,
        List<Long> itemIds,
        List<Long> variantIds,
        List<Long> excludedVariantIds,
        LocalDateTime createdAt
) {
}
