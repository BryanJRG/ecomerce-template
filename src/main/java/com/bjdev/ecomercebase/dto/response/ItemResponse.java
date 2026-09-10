package com.bjdev.ecomercebase.dto.response;

import java.util.List;

public record ItemResponse(
        Long id,
        String name,
        String description,
        Long categoryId,
        Long brandId,
        Boolean active,
        Integer totalStock,
        List<ItemVariantResponse> variants
) {
}
