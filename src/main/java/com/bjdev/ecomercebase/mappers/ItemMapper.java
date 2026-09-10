package com.bjdev.ecomercebase.mappers;

import com.bjdev.ecomercebase.dto.request.ItemUpdateRequest;
import com.bjdev.ecomercebase.dto.response.ItemResponse;
import com.bjdev.ecomercebase.dto.response.ItemVariantResponse;
import com.bjdev.ecomercebase.models.catalog.Item;
import com.bjdev.ecomercebase.models.catalog.ItemVariant;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ItemMapper {

    /**
     * Applies only the non-null scalar fields of the request onto the existing entity — no
     * manual if/else. category/brand/variant prices are relations/cross-entity validations
     * handled by ItemServiceImpl, so they're excluded here on purpose.
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "brand", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "totalStock", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateItemFromRequest(ItemUpdateRequest request, @MappingTarget Item item);

    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "brandId", source = "brand.id")
    @Mapping(target = "variants", ignore = true)
    ItemResponse toResponse(Item item);

    ItemVariantResponse toVariantResponse(ItemVariant variant);

    List<ItemVariantResponse> toVariantResponses(List<ItemVariant> variants);
}
