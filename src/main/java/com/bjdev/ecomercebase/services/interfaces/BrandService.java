package com.bjdev.ecomercebase.services.interfaces;

import com.bjdev.ecomercebase.dto.request.BrandCreateRequest;
import com.bjdev.ecomercebase.dto.request.BrandUpdateRequest;
import com.bjdev.ecomercebase.dto.response.BrandResponse;
import com.bjdev.ecomercebase.models.catalog.Brand;

import java.util.List;

public interface BrandService {

    BrandResponse createBrand(BrandCreateRequest request);

    BrandResponse getBrand(Long id);

    /** Full list, cached as a whole — see CatalogCacheService. */
    List<BrandResponse> listBrands();

    BrandResponse updateBrand(Long id, BrandUpdateRequest request);

    /**
     * Soft-delete: toggles Brand.active = false. Throws CatalogException.brandInUse if any
     * active Item still references it — those must be reassigned first.
     */
    BrandResponse deactivateBrand(Long id);

    /** Reverses deactivateBrand. */
    BrandResponse activateBrand(Long id);

    /** Loaded by ItemServiceImpl to validate active status when a brand is given. */
    Brand getActiveBrandOrThrow(Long id);
}
