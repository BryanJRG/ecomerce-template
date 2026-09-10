package com.bjdev.ecomercebase.services.impl.catalog;

import com.bjdev.ecomercebase.dto.request.ItemCreateRequest;
import com.bjdev.ecomercebase.dto.request.ItemUpdateRequest;
import com.bjdev.ecomercebase.dto.request.ItemVariantPriceUpdate;
import com.bjdev.ecomercebase.dto.request.ItemVariantRequest;
import com.bjdev.ecomercebase.dto.response.ItemResponse;
import com.bjdev.ecomercebase.events.EntityAction;
import com.bjdev.ecomercebase.events.EntityChangedEvent;
import com.bjdev.ecomercebase.exception.CatalogException;
import com.bjdev.ecomercebase.mappers.ItemMapper;
import com.bjdev.ecomercebase.models.catalog.Brand;
import com.bjdev.ecomercebase.models.catalog.Category;
import com.bjdev.ecomercebase.models.catalog.Item;
import com.bjdev.ecomercebase.models.catalog.ItemVariant;
import com.bjdev.ecomercebase.models.user.User;
import com.bjdev.ecomercebase.repositories.catalog.ItemRepository;
import com.bjdev.ecomercebase.repositories.catalog.ItemVariantRepository;
import com.bjdev.ecomercebase.security.CurrentUserProvider;
import com.bjdev.ecomercebase.services.impl.audit.AuditService;
import com.bjdev.ecomercebase.services.interfaces.BrandService;
import com.bjdev.ecomercebase.services.interfaces.CategoryService;
import com.bjdev.ecomercebase.services.interfaces.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private static final String ENTITY_TYPE = "ITEM";
    private static final String TABLE_NAME = "items";

    private final ItemRepository itemRepository;
    private final ItemVariantRepository itemVariantRepository;
    private final CategoryService categoryService;
    private final BrandService brandService;
    private final ItemMapper itemMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final AuditService auditService;
    private final CurrentUserProvider currentUserProvider;
    private final CatalogCacheService catalogCacheService;

    @Override
    @Transactional
    public ItemResponse createItem(ItemCreateRequest request) {
        Category category = categoryService.getActiveCategoryOrThrow(request.categoryId());
        Brand brand = request.brandId() != null ? brandService.getActiveBrandOrThrow(request.brandId()) : null;

        List<ItemVariantRequest> variantRequests = (request.variants() == null || request.variants().isEmpty())
                ? List.of(new ItemVariantRequest(request.sku(), request.name(), request.price(),
                        request.stock(), request.lowStockThreshold()))
                : request.variants();

        variantRequests.forEach(v -> validateVariantPrice(v.price(), category, v.name()));

        Item item = Item.builder()
                .name(request.name())
                .description(request.description())
                .category(category)
                .brand(brand)
                .build();
        item = itemRepository.save(item);

        Item savedItem = item;
        List<ItemVariant> variants = variantRequests.stream()
                .map(v -> toVariantEntity(v, savedItem))
                .toList();
        variants = itemVariantRepository.saveAll(variants);

        item.setTotalStock(sumStock(variants));
        item = itemRepository.save(item);

        recordChange(EntityAction.CREATED, item.getId(), item.getName());

        return toResponse(item, variants);
    }

    @Override
    @Transactional
    public ItemResponse updateItem(Long id, ItemUpdateRequest request) {
        Item item = getItemOrThrow(id);

        Category effectiveCategory = item.getCategory();
        if (request.categoryId() != null) {
            effectiveCategory = categoryService.getActiveCategoryOrThrow(request.categoryId());
            item.setCategory(effectiveCategory);
        }

        if (request.brandId() != null) {
            item.setBrand(brandService.getActiveBrandOrThrow(request.brandId()));
        }

        itemMapper.updateItemFromRequest(request, item);

        if (request.variantPriceUpdates() != null && !request.variantPriceUpdates().isEmpty()) {
            applyVariantPriceUpdates(item, request.variantPriceUpdates(), effectiveCategory);
        }

        item = itemRepository.save(item);
        List<ItemVariant> variants = itemVariantRepository.findByItemId(item.getId());
        item.setTotalStock(sumStock(variants));
        item = itemRepository.save(item);

        recordChange(EntityAction.UPDATED, item.getId(), item.getName());

        return toResponse(item, variants);
    }

    @Override
    @Transactional
    public ItemResponse deactivateItem(Long id) {
        Item item = getItemOrThrow(id);
        item.setActive(false);
        item = itemRepository.save(item);

        recordChange(EntityAction.DEACTIVATED, item.getId(), item.getName());

        return toResponse(item, itemVariantRepository.findByItemId(item.getId()));
    }

    @Override
    @Transactional
    public ItemResponse activateItem(Long id) {
        Item item = getItemOrThrow(id);
        item.setActive(true);
        item = itemRepository.save(item);

        recordChange(EntityAction.ACTIVATED, item.getId(), item.getName());

        return toResponse(item, itemVariantRepository.findByItemId(item.getId()));
    }

    @Override
    public ItemResponse getItem(Long id) {
        return catalogCacheService.getItem(id).orElseGet(() -> {
            Item item = getItemOrThrow(id);
            ItemResponse response = toResponse(item, itemVariantRepository.findByItemId(item.getId()));
            catalogCacheService.putItem(id, response);
            return response;
        });
    }

    @Override
    public Page<ItemResponse> searchItems(Long categoryId, Long brandId, String name, BigDecimal minPrice,
                                           BigDecimal maxPrice, Pageable pageable) {
        Page<Item> items = itemRepository.search(categoryId, brandId, name, minPrice, maxPrice, pageable);

        List<Long> itemIds = items.getContent().stream().map(Item::getId).toList();
        Map<Long, List<ItemVariant>> variantsByItemId = itemVariantRepository.findByItemIdIn(itemIds).stream()
                .collect(Collectors.groupingBy(v -> v.getItem().getId()));

        return items.map(item -> toResponse(item, variantsByItemId.getOrDefault(item.getId(), List.of())));
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

    private void applyVariantPriceUpdates(Item item, List<ItemVariantPriceUpdate> updates, Category category) {
        for (ItemVariantPriceUpdate update : updates) {
            ItemVariant variant = itemVariantRepository.findById(update.variantId())
                    .orElseThrow(CatalogException::variantNotFound);
            if (!variant.getItem().getId().equals(item.getId())) {
                throw CatalogException.variantNotFound();
            }
            validateVariantPrice(update.price(), category, variant.getName());
            variant.setPrice(update.price());
            itemVariantRepository.save(variant);
        }
    }

    private void validateVariantPrice(BigDecimal price, Category category, String variantLabel) {
        BigDecimal min = category.getMinPrice();
        BigDecimal max = category.getMaxPrice();
        boolean belowMin = min != null && price.compareTo(min) < 0;
        boolean aboveMax = max != null && price.compareTo(max) > 0;
        if (belowMin || aboveMax) {
            throw CatalogException.priceOutOfRange(variantLabel, min, max);
        }
    }

    private ItemVariant toVariantEntity(ItemVariantRequest request, Item item) {
        return ItemVariant.builder()
                .item(item)
                .sku(request.sku())
                .name(request.name())
                .price(request.price())
                .stock(request.stock())
                .lowStockThreshold(request.lowStockThreshold() != null ? request.lowStockThreshold() : 5)
                .build();
    }

    private Integer sumStock(List<ItemVariant> variants) {
        return variants.stream().mapToInt(ItemVariant::getStock).sum();
    }

    private Item getItemOrThrow(Long id) {
        return itemRepository.findById(id).orElseThrow(CatalogException::itemNotFound);
    }

    private ItemResponse toResponse(Item item, List<ItemVariant> variants) {
        ItemResponse base = itemMapper.toResponse(item);
        return new ItemResponse(base.id(), base.name(), base.description(), base.categoryId(), base.brandId(),
                base.active(), item.getTotalStock(), itemMapper.toVariantResponses(variants));
    }
}
