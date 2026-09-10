package com.bjdev.ecomercebase.mappers;

import com.bjdev.ecomercebase.dto.request.DiscountUpdateRequest;
import com.bjdev.ecomercebase.dto.response.DiscountResponse;
import com.bjdev.ecomercebase.models.discount.Discount;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DiscountMapper {

    /**
     * Applies only the non-null scalar fields of the request onto the existing entity. Targeting
     * (applyToAllCatalog/categoryIds/itemIds/variantIds/excludedVariantIds) has no matching entity
     * field — it's resolved into targetType + the targeting/exclusion tables by DiscountServiceImpl.
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "targetType", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateDiscountFromRequest(DiscountUpdateRequest request, @MappingTarget Discount discount);

    /** categoryIds/itemIds/variantIds/excludedVariantIds come from the targeting tables, resolved by the service. */
    DiscountResponse toResponse(Discount discount, List<Long> categoryIds, List<Long> itemIds,
                                 List<Long> variantIds, List<Long> excludedVariantIds);
}
