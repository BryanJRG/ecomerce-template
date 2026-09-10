package com.bjdev.ecomercebase.dto.request;

import com.bjdev.ecomercebase.models.enums.DiscountType;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Low-friction contract for the admin: targeting is expressed as flags/id lists here, and the
 * service (a later task) translates it into the canonical {@code Discount.targetType} plus,
 * optionally, {@code DiscountExcludedVariant} rows — see {@code Discount}'s class doc.
 * <p>
 * The {@code @AssertTrue} methods below document the intended shape; the actual business
 * validation is implemented in the service.
 */
public record DiscountCreateRequest(
        @NotBlank String title,
        @NotNull DiscountType type,
        @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal value,
        @NotNull LocalDateTime startDate,
        @NotNull LocalDateTime endDate,
        String code,
        @DecimalMin(value = "0.0") BigDecimal minPurchaseAmount,
        @Positive Integer maxUsesTotal,
        @Positive Integer maxUsesPerClient,
        boolean applyToAllCatalog,
        List<Long> categoryIds,
        List<Long> itemIds,
        List<Long> variantIds,
        List<Long> excludedVariantIds
) {

    /** Exactly one of {applyToAllCatalog, categoryIds, itemIds, variantIds} must be present — never zero, never more than one. */
    @AssertTrue(message = "exactly one targeting option (applyToAllCatalog, categoryIds, itemIds or variantIds) must be provided")
    public boolean isTargetingValid() {
        boolean all = applyToAllCatalog;
        boolean category = categoryIds != null && !categoryIds.isEmpty();
        boolean item = itemIds != null && !itemIds.isEmpty();
        boolean variant = variantIds != null && !variantIds.isEmpty();
        int selected = (all ? 1 : 0) + (category ? 1 : 0) + (item ? 1 : 0) + (variant ? 1 : 0);
        return selected == 1;
    }

    /** excludedVariantIds is only valid when variantIds is NOT used — VARIANT targeting is already explicit inclusion. */
    @AssertTrue(message = "excludedVariantIds cannot be combined with variantIds")
    public boolean isExclusionValid() {
        boolean variant = variantIds != null && !variantIds.isEmpty();
        boolean excluded = excludedVariantIds != null && !excludedVariantIds.isEmpty();
        return !(variant && excluded);
    }
}
