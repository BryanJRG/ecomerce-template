package com.bjdev.ecomercebase.services.interfaces;

import com.bjdev.ecomercebase.dto.request.CategoryCreateRequest;
import com.bjdev.ecomercebase.dto.request.CategoryUpdateRequest;
import com.bjdev.ecomercebase.dto.response.CategoryResponse;
import com.bjdev.ecomercebase.models.catalog.Category;

import java.util.List;

public interface CategoryService {

    CategoryResponse createCategory(CategoryCreateRequest request);

    CategoryResponse getCategory(Long id);

    /** Full list, cached as a whole — see CatalogCacheService. */
    List<CategoryResponse> listCategories();

    CategoryResponse updateCategory(Long id, CategoryUpdateRequest request);

    /**
     * Soft-delete: toggles Category.active = false. Throws CatalogException.categoryInUse if any
     * active Item still references it — those must be reassigned first.
     */
    CategoryResponse deactivateCategory(Long id);

    /** Reverses deactivateCategory. */
    CategoryResponse activateCategory(Long id);

    /** Loaded by ItemServiceImpl to validate active status and price bounds. */
    Category getActiveCategoryOrThrow(Long id);
}
