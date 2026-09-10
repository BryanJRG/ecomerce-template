package com.bjdev.ecomercebase.services.impl.catalog;

import com.bjdev.ecomercebase.dto.request.CategoryCreateRequest;
import com.bjdev.ecomercebase.dto.request.CategoryUpdateRequest;
import com.bjdev.ecomercebase.dto.response.CategoryResponse;
import com.bjdev.ecomercebase.events.EntityAction;
import com.bjdev.ecomercebase.events.EntityChangedEvent;
import com.bjdev.ecomercebase.exception.CatalogException;
import com.bjdev.ecomercebase.models.catalog.Category;
import com.bjdev.ecomercebase.models.catalog.Item;
import com.bjdev.ecomercebase.models.user.User;
import com.bjdev.ecomercebase.repositories.catalog.CategoryRepository;
import com.bjdev.ecomercebase.repositories.catalog.ItemRepository;
import com.bjdev.ecomercebase.security.CurrentUserProvider;
import com.bjdev.ecomercebase.services.impl.audit.AuditService;
import com.bjdev.ecomercebase.services.interfaces.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private static final String ENTITY_TYPE = "CATEGORY";
    private static final String TABLE_NAME = "categories";

    private final CategoryRepository categoryRepository;
    private final ItemRepository itemRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final AuditService auditService;
    private final CurrentUserProvider currentUserProvider;
    private final CatalogCacheService catalogCacheService;

    @Override
    @Transactional
    public CategoryResponse createCategory(CategoryCreateRequest request) {
        Category category = Category.builder()
                .name(request.name())
                .slug(request.slug())
                .minPrice(request.minPrice())
                .maxPrice(request.maxPrice())
                .build();
        category = categoryRepository.save(category);

        recordChange(EntityAction.CREATED, category.getId(), category.getName());

        return toResponse(category);
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryUpdateRequest request) {
        Category category = getById(id);

        if (request.name() != null) category.setName(request.name());
        if (request.slug() != null) category.setSlug(request.slug());
        if (request.minPrice() != null) category.setMinPrice(request.minPrice());
        if (request.maxPrice() != null) category.setMaxPrice(request.maxPrice());
        category = categoryRepository.save(category);

        recordChange(EntityAction.UPDATED, category.getId(), category.getName());

        return toResponse(category);
    }

    @Override
    @Transactional
    public CategoryResponse deactivateCategory(Long id) {
        Category category = getById(id);

        List<Item> blockingItems = itemRepository.findByCategoryIdAndActiveTrue(id);
        if (!blockingItems.isEmpty()) {
            throw CatalogException.categoryInUse(category.getName(), blockingItems);
        }

        category.setActive(false);
        category = categoryRepository.save(category);

        recordChange(EntityAction.DEACTIVATED, category.getId(), category.getName());

        return toResponse(category);
    }

    @Override
    @Transactional
    public CategoryResponse activateCategory(Long id) {
        Category category = getById(id);
        category.setActive(true);
        category = categoryRepository.save(category);

        recordChange(EntityAction.ACTIVATED, category.getId(), category.getName());

        return toResponse(category);
    }

    @Override
    public Category getActiveCategoryOrThrow(Long id) {
        Category category = getById(id);
        if (!Boolean.TRUE.equals(category.getActive())) {
            throw CatalogException.categoryInactive();
        }
        return category;
    }

    @Override
    public CategoryResponse getCategory(Long id) {
        return toResponse(getById(id));
    }

    @Override
    public List<CategoryResponse> listCategories() {
        return catalogCacheService.getCategories().orElseGet(() -> {
            List<CategoryResponse> categories = categoryRepository.findAll().stream().map(this::toResponse).toList();
            catalogCacheService.putCategories(categories);
            return categories;
        });
    }

    /**
     * Synchronous audit (must never be lost, even if it means failing the request) + cache-eviction/
     * notification via EntityChangedEvent — see EntityCacheEvictionListener and
     * EntityChangeNotificationListener, the generic listeners every admin-managed entity shares.
     */
    private void recordChange(EntityAction action, Long entityId, String entityLabel) {
        User actor = currentUserProvider.getCurrentUserOrNull();
        auditService.record(actor, ENTITY_TYPE + "_" + action, TABLE_NAME, entityId, null, null, null);
        eventPublisher.publishEvent(new EntityChangedEvent(
                ENTITY_TYPE, action, entityId, entityLabel, actor != null ? actor.getId() : null));
    }

    private Category getById(Long id) {
        return categoryRepository.findById(id).orElseThrow(CatalogException::categoryNotFound);
    }

    private CategoryResponse toResponse(Category category) {
        return new CategoryResponse(category.getId(), category.getName(), category.getSlug(),
                category.getActive(), category.getMinPrice(), category.getMaxPrice());
    }
}
