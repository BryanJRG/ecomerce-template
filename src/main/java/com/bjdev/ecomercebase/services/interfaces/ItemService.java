package com.bjdev.ecomercebase.services.interfaces;

import com.bjdev.ecomercebase.dto.request.ItemCreateRequest;
import com.bjdev.ecomercebase.dto.request.ItemUpdateRequest;
import com.bjdev.ecomercebase.dto.response.ItemResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface ItemService {

    ItemResponse createItem(ItemCreateRequest request);

    ItemResponse updateItem(Long id, ItemUpdateRequest request);

    /** Soft-delete: toggles Item.active = false. Never removes the row. */
    ItemResponse deactivateItem(Long id);

    /** Reverses deactivateItem. */
    ItemResponse activateItem(Long id);

    ItemResponse getItem(Long id);

    /** Public catalog browsing — active items only, optionally filtered by category/brand/name/price range. */
    Page<ItemResponse> searchItems(Long categoryId, Long brandId, String name, BigDecimal minPrice,
                                    BigDecimal maxPrice, Pageable pageable);
}
