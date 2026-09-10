package com.bjdev.ecomercebase.services.impl.discount;

import com.bjdev.ecomercebase.dto.request.DiscountCreateRequest;
import com.bjdev.ecomercebase.dto.request.DiscountUpdateRequest;
import com.bjdev.ecomercebase.dto.response.DiscountResponse;
import com.bjdev.ecomercebase.events.EntityAction;
import com.bjdev.ecomercebase.events.EntityChangedEvent;
import com.bjdev.ecomercebase.exception.CatalogException;
import com.bjdev.ecomercebase.exception.DiscountException;
import com.bjdev.ecomercebase.mappers.DiscountMapper;
import com.bjdev.ecomercebase.models.catalog.Category;
import com.bjdev.ecomercebase.models.catalog.Item;
import com.bjdev.ecomercebase.models.catalog.ItemVariant;
import com.bjdev.ecomercebase.models.discount.Discount;
import com.bjdev.ecomercebase.models.discount.DiscountCategory;
import com.bjdev.ecomercebase.models.discount.DiscountExcludedVariant;
import com.bjdev.ecomercebase.models.discount.DiscountItem;
import com.bjdev.ecomercebase.models.discount.DiscountVariant;
import com.bjdev.ecomercebase.models.enums.DiscountTargetType;
import com.bjdev.ecomercebase.models.user.User;
import com.bjdev.ecomercebase.repositories.catalog.CategoryRepository;
import com.bjdev.ecomercebase.repositories.catalog.ItemRepository;
import com.bjdev.ecomercebase.repositories.catalog.ItemVariantRepository;
import com.bjdev.ecomercebase.repositories.discount.DiscountCategoryRepository;
import com.bjdev.ecomercebase.repositories.discount.DiscountExcludedVariantRepository;
import com.bjdev.ecomercebase.repositories.discount.DiscountItemRepository;
import com.bjdev.ecomercebase.repositories.discount.DiscountRepository;
import com.bjdev.ecomercebase.repositories.discount.DiscountVariantRepository;
import com.bjdev.ecomercebase.security.CurrentUserProvider;
import com.bjdev.ecomercebase.services.impl.audit.AuditService;
import com.bjdev.ecomercebase.services.interfaces.DiscountService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DiscountServiceImpl implements DiscountService {

    private static final String ENTITY_TYPE = "DISCOUNT";
    private static final String TABLE_NAME = "discounts";

    private final DiscountRepository discountRepository;
    private final DiscountCategoryRepository discountCategoryRepository;
    private final DiscountItemRepository discountItemRepository;
    private final DiscountVariantRepository discountVariantRepository;
    private final DiscountExcludedVariantRepository discountExcludedVariantRepository;
    private final CategoryRepository categoryRepository;
    private final ItemRepository itemRepository;
    private final ItemVariantRepository itemVariantRepository;
    private final DiscountMapper discountMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final AuditService auditService;
    private final CurrentUserProvider currentUserProvider;

    @Override
    @Transactional
    public DiscountResponse createDiscount(DiscountCreateRequest request) {
        validateDateRange(request.startDate(), request.endDate());
        validateCodeAvailable(request.code(), null);

        DiscountTargetType targetType = resolveTargetType(request.applyToAllCatalog(),
                request.categoryIds(), request.itemIds(), request.variantIds());
        validateExclusion(targetType, request.excludedVariantIds());

        Discount discount = Discount.builder()
                .title(request.title())
                .type(request.type())
                .value(request.value())
                .targetType(targetType)
                .startDate(request.startDate())
                .endDate(request.endDate())
                .code(request.code())
                .minPurchaseAmount(request.minPurchaseAmount())
                .maxUsesTotal(request.maxUsesTotal())
                .maxUsesPerClient(request.maxUsesPerClient())
                .build();
        discount = discountRepository.save(discount);

        applyTargeting(discount, targetType, request.categoryIds(), request.itemIds(), request.variantIds());
        applyExclusions(discount, request.excludedVariantIds());

        recordChange(EntityAction.CREATED, discount.getId(), discount.getTitle());

        return toResponse(discount);
    }

    @Override
    @Transactional
    public DiscountResponse updateDiscount(Long id, DiscountUpdateRequest request) {
        Discount discount = getByIdOrThrow(id);

        LocalDateTime effectiveStart = request.startDate() != null ? request.startDate() : discount.getStartDate();
        LocalDateTime effectiveEnd = request.endDate() != null ? request.endDate() : discount.getEndDate();
        if (request.startDate() != null || request.endDate() != null) {
            validateDateRange(effectiveStart, effectiveEnd);
        }
        validateCodeAvailable(request.code(), discount.getCode());

        discountMapper.updateDiscountFromRequest(request, discount);

        boolean retargeting = Boolean.TRUE.equals(request.applyToAllCatalog())
                || hasContent(request.categoryIds()) || hasContent(request.itemIds()) || hasContent(request.variantIds());
        if (retargeting) {
            DiscountTargetType targetType = resolveTargetType(Boolean.TRUE.equals(request.applyToAllCatalog()),
                    request.categoryIds(), request.itemIds(), request.variantIds());
            validateExclusion(targetType, request.excludedVariantIds());

            discount.setTargetType(targetType);
            clearTargeting(discount.getId());
            applyTargeting(discount, targetType, request.categoryIds(), request.itemIds(), request.variantIds());

            if (targetType == DiscountTargetType.VARIANT) {
                discountExcludedVariantRepository.deleteByDiscountId(discount.getId());
            } else if (request.excludedVariantIds() != null) {
                discountExcludedVariantRepository.deleteByDiscountId(discount.getId());
                applyExclusions(discount, request.excludedVariantIds());
            }
        } else if (request.excludedVariantIds() != null) {
            validateExclusion(discount.getTargetType(), request.excludedVariantIds());
            discountExcludedVariantRepository.deleteByDiscountId(discount.getId());
            applyExclusions(discount, request.excludedVariantIds());
        }

        discount = discountRepository.save(discount);

        recordChange(EntityAction.UPDATED, discount.getId(), discount.getTitle());

        return toResponse(discount);
    }

    /**
     * Synchronous audit (must never be lost, even if it means failing the request) + notification
     * via EntityChangedEvent — see EntityChangeNotificationListener, the generic listener every
     * admin-managed entity shares. EntityCacheEvictionListener no-ops for DISCOUNT: no cache is
     * defined for it yet.
     */
    private void recordChange(EntityAction action, Long entityId, String entityLabel) {
        User actor = currentUserProvider.getCurrentUserOrNull();
        auditService.record(actor, ENTITY_TYPE + "_" + action, TABLE_NAME, entityId, null, null, null);
        eventPublisher.publishEvent(new EntityChangedEvent(
                ENTITY_TYPE, action, entityId, entityLabel, actor != null ? actor.getId() : null));
    }

    @Override
    public DiscountResponse getDiscount(Long id) {
        return toResponse(getByIdOrThrow(id));
    }

    @Override
    public List<DiscountResponse> listDiscounts() {
        return discountRepository.findAll().stream().map(this::toResponse).toList();
    }

    private DiscountTargetType resolveTargetType(boolean applyToAll, List<Long> categoryIds, List<Long> itemIds,
                                                  List<Long> variantIds) {
        boolean all = applyToAll;
        boolean category = hasContent(categoryIds);
        boolean item = hasContent(itemIds);
        boolean variant = hasContent(variantIds);
        int selected = (all ? 1 : 0) + (category ? 1 : 0) + (item ? 1 : 0) + (variant ? 1 : 0);
        if (selected != 1) {
            throw DiscountException.invalidTargeting();
        }
        if (category) return DiscountTargetType.CATEGORY;
        if (item) return DiscountTargetType.ITEM;
        if (variant) return DiscountTargetType.VARIANT;
        return DiscountTargetType.ALL;
    }

    private void validateExclusion(DiscountTargetType targetType, List<Long> excludedVariantIds) {
        if (targetType == DiscountTargetType.VARIANT && hasContent(excludedVariantIds)) {
            throw DiscountException.invalidExclusion();
        }
    }

    private void validateDateRange(LocalDateTime start, LocalDateTime end) {
        if (start != null && end != null && !end.isAfter(start)) {
            throw DiscountException.invalidDateRange();
        }
    }

    /** currentCode is the discount's own code being updated (null on create) — a no-op rename is not a conflict. */
    private void validateCodeAvailable(String code, String currentCode) {
        if (code == null || code.equals(currentCode)) return;
        if (discountRepository.existsByCode(code)) {
            throw DiscountException.codeAlreadyInUse(code);
        }
    }

    private void applyTargeting(Discount discount, DiscountTargetType targetType, List<Long> categoryIds,
                                 List<Long> itemIds, List<Long> variantIds) {
        switch (targetType) {
            case CATEGORY -> {
                List<Category> categories = categoryRepository.findAllById(categoryIds);
                if (categories.size() != categoryIds.size()) throw CatalogException.categoryNotFound();
                discountCategoryRepository.saveAll(categories.stream()
                        .map(c -> DiscountCategory.builder().discount(discount).category(c).build())
                        .toList());
            }
            case ITEM -> {
                List<Item> items = itemRepository.findAllById(itemIds);
                if (items.size() != itemIds.size()) throw CatalogException.itemNotFound();
                discountItemRepository.saveAll(items.stream()
                        .map(i -> DiscountItem.builder().discount(discount).item(i).build())
                        .toList());
            }
            case VARIANT -> {
                List<ItemVariant> variants = itemVariantRepository.findAllById(variantIds);
                if (variants.size() != variantIds.size()) throw CatalogException.variantNotFound();
                discountVariantRepository.saveAll(variants.stream()
                        .map(v -> DiscountVariant.builder().discount(discount).variant(v).build())
                        .toList());
            }
            case ALL -> {
                // No targeting rows: an ALL-catalog discount only needs the exclusion table (if any).
            }
        }
    }

    private void applyExclusions(Discount discount, List<Long> excludedVariantIds) {
        if (excludedVariantIds == null || excludedVariantIds.isEmpty()) return;
        List<ItemVariant> variants = itemVariantRepository.findAllById(excludedVariantIds);
        if (variants.size() != excludedVariantIds.size()) throw CatalogException.variantNotFound();
        discountExcludedVariantRepository.saveAll(variants.stream()
                .map(v -> DiscountExcludedVariant.builder().discount(discount).variant(v).build())
                .toList());
    }

    private void clearTargeting(Long discountId) {
        discountCategoryRepository.deleteByDiscountId(discountId);
        discountItemRepository.deleteByDiscountId(discountId);
        discountVariantRepository.deleteByDiscountId(discountId);
    }

    private boolean hasContent(List<Long> list) {
        return list != null && !list.isEmpty();
    }

    private Discount getByIdOrThrow(Long id) {
        return discountRepository.findById(id).orElseThrow(DiscountException::discountNotFound);
    }

    private DiscountResponse toResponse(Discount discount) {
        List<Long> categoryIds = discountCategoryRepository.findByDiscountId(discount.getId()).stream()
                .map(dc -> dc.getCategory().getId()).toList();
        List<Long> itemIds = discountItemRepository.findByDiscountId(discount.getId()).stream()
                .map(di -> di.getItem().getId()).toList();
        List<Long> variantIds = discountVariantRepository.findByDiscountId(discount.getId()).stream()
                .map(dv -> dv.getVariant().getId()).toList();
        List<Long> excludedVariantIds = discountExcludedVariantRepository.findByDiscountId(discount.getId()).stream()
                .map(dev -> dev.getVariant().getId()).toList();
        return discountMapper.toResponse(discount, categoryIds, itemIds, variantIds, excludedVariantIds);
    }
}
