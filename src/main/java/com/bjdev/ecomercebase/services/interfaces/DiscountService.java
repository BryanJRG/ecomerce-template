package com.bjdev.ecomercebase.services.interfaces;

import com.bjdev.ecomercebase.dto.request.DiscountCreateRequest;
import com.bjdev.ecomercebase.dto.request.DiscountUpdateRequest;
import com.bjdev.ecomercebase.dto.response.DiscountResponse;

import java.util.List;

public interface DiscountService {

    DiscountResponse createDiscount(DiscountCreateRequest request);

    /**
     * Partial update. Providing any of {applyToAllCatalog, categoryIds, itemIds, variantIds}
     * replaces the discount's targeting entirely (old targeting rows are deleted first) — exactly
     * one of them must resolve, same rule as create. excludedVariantIds, when provided, replaces
     * the exclusion set independently of whether targeting changed.
     */
    DiscountResponse updateDiscount(Long id, DiscountUpdateRequest request);

    DiscountResponse getDiscount(Long id);

    List<DiscountResponse> listDiscounts();
}
