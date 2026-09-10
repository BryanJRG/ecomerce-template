package com.bjdev.ecomercebase.services.impl.catalog;

import com.bjdev.ecomercebase.dto.request.BrandCreateRequest;
import com.bjdev.ecomercebase.dto.request.BrandUpdateRequest;
import com.bjdev.ecomercebase.dto.response.BrandResponse;
import com.bjdev.ecomercebase.events.EntityAction;
import com.bjdev.ecomercebase.events.EntityChangedEvent;
import com.bjdev.ecomercebase.exception.CatalogException;
import com.bjdev.ecomercebase.models.catalog.Brand;
import com.bjdev.ecomercebase.models.catalog.Item;
import com.bjdev.ecomercebase.models.user.User;
import com.bjdev.ecomercebase.repositories.catalog.BrandRepository;
import com.bjdev.ecomercebase.repositories.catalog.ItemRepository;
import com.bjdev.ecomercebase.security.CurrentUserProvider;
import com.bjdev.ecomercebase.services.impl.audit.AuditService;
import com.bjdev.ecomercebase.services.interfaces.BrandService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BrandServiceImpl implements BrandService {

    private static final String ENTITY_TYPE = "BRAND";
    private static final String TABLE_NAME = "brands";

    private final BrandRepository brandRepository;
    private final ItemRepository itemRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final AuditService auditService;
    private final CurrentUserProvider currentUserProvider;
    private final CatalogCacheService catalogCacheService;

    @Override
    @Transactional
    public BrandResponse createBrand(BrandCreateRequest request) {
        Brand brand = Brand.builder()
                .name(request.name())
                .logoUrl(request.logoUrl())
                .build();
        brand = brandRepository.save(brand);

        recordChange(EntityAction.CREATED, brand.getId(), brand.getName());

        return toResponse(brand);
    }

    @Override
    @Transactional
    public BrandResponse updateBrand(Long id, BrandUpdateRequest request) {
        Brand brand = getById(id);

        if (request.name() != null) brand.setName(request.name());
        if (request.logoUrl() != null) brand.setLogoUrl(request.logoUrl());
        brand = brandRepository.save(brand);

        recordChange(EntityAction.UPDATED, brand.getId(), brand.getName());

        return toResponse(brand);
    }

    @Override
    @Transactional
    public BrandResponse deactivateBrand(Long id) {
        Brand brand = getById(id);

        List<Item> blockingItems = itemRepository.findByBrandIdAndActiveTrue(id);
        if (!blockingItems.isEmpty()) {
            throw CatalogException.brandInUse(brand.getName(), blockingItems);
        }

        brand.setActive(false);
        brand = brandRepository.save(brand);

        recordChange(EntityAction.DEACTIVATED, brand.getId(), brand.getName());

        return toResponse(brand);
    }

    @Override
    @Transactional
    public BrandResponse activateBrand(Long id) {
        Brand brand = getById(id);
        brand.setActive(true);
        brand = brandRepository.save(brand);

        recordChange(EntityAction.ACTIVATED, brand.getId(), brand.getName());

        return toResponse(brand);
    }

    @Override
    public Brand getActiveBrandOrThrow(Long id) {
        Brand brand = getById(id);
        if (!Boolean.TRUE.equals(brand.getActive())) {
            throw CatalogException.brandInactive();
        }
        return brand;
    }

    @Override
    public BrandResponse getBrand(Long id) {
        return toResponse(getById(id));
    }

    @Override
    public List<BrandResponse> listBrands() {
        return catalogCacheService.getBrands().orElseGet(() -> {
            List<BrandResponse> brands = brandRepository.findAll().stream().map(this::toResponse).toList();
            catalogCacheService.putBrands(brands);
            return brands;
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

    private Brand getById(Long id) {
        return brandRepository.findById(id).orElseThrow(CatalogException::brandNotFound);
    }

    private BrandResponse toResponse(Brand brand) {
        return new BrandResponse(brand.getId(), brand.getName(), brand.getLogoUrl(), brand.getActive());
    }
}
