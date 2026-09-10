package com.bjdev.ecomercebase.services.impl.discount;

import com.bjdev.ecomercebase.dto.request.DiscountCreateRequest;
import com.bjdev.ecomercebase.exception.DiscountException;
import com.bjdev.ecomercebase.mappers.DiscountMapper;
import com.bjdev.ecomercebase.models.discount.Discount;
import com.bjdev.ecomercebase.models.enums.DiscountTargetType;
import com.bjdev.ecomercebase.models.enums.DiscountType;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Targeting/exclusion/date-range/code-uniqueness are DiscountServiceImpl's own business rules
 * (the entity and DTOs only document the shape) — these are pure unit tests, mocking every
 * repository/collaborator, since the rules under test never touch the database themselves.
 */
@ExtendWith(MockitoExtension.class)
class DiscountServiceImplTest {

    @Mock
    private DiscountRepository discountRepository;
    @Mock
    private DiscountCategoryRepository discountCategoryRepository;
    @Mock
    private DiscountItemRepository discountItemRepository;
    @Mock
    private DiscountVariantRepository discountVariantRepository;
    @Mock
    private DiscountExcludedVariantRepository discountExcludedVariantRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private ItemVariantRepository itemVariantRepository;
    @Mock
    private DiscountMapper discountMapper;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private AuditService auditService;
    @Mock
    private CurrentUserProvider currentUserProvider;

    private DiscountServiceImpl service() {
        return new DiscountServiceImpl(discountRepository, discountCategoryRepository, discountItemRepository,
                discountVariantRepository, discountExcludedVariantRepository, categoryRepository, itemRepository,
                itemVariantRepository, discountMapper, eventPublisher, auditService, currentUserProvider);
    }

    private static DiscountCreateRequest requestWithTargeting(boolean applyToAll, List<Long> categoryIds,
                                                                List<Long> itemIds, List<Long> variantIds,
                                                                List<Long> excludedVariantIds) {
        return new DiscountCreateRequest("Sale", DiscountType.PERCENTAGE, BigDecimal.TEN,
                LocalDateTime.now(), LocalDateTime.now().plusDays(7), null, null, null, null,
                applyToAll, categoryIds, itemIds, variantIds, excludedVariantIds);
    }

    @Test
    void createDiscount_applyToAllCatalog_persistsAllTargetType() {
        when(discountRepository.save(any(Discount.class))).thenAnswer(inv -> {
            Discount d = inv.getArgument(0);
            d.setId(1L);
            return d;
        });
        when(currentUserProvider.getCurrentUserOrNull()).thenReturn(null);

        service().createDiscount(requestWithTargeting(true, null, null, null, null));

        ArgumentCaptor<Discount> captor = ArgumentCaptor.forClass(Discount.class);
        verify(discountRepository).save(captor.capture());
        assertThat(captor.getValue().getTargetType()).isEqualTo(DiscountTargetType.ALL);
        verifyNoInteractions(categoryRepository, itemRepository, itemVariantRepository);
    }

    @Test
    void createDiscount_noTargetingOptionSelected_throwsInvalidTargeting() {
        assertThatThrownBy(() -> service().createDiscount(requestWithTargeting(false, null, null, null, null)))
                .isInstanceOfSatisfying(DiscountException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo("DISCOUNT_INVALID_TARGETING"));
        verifyNoInteractions(discountRepository);
    }

    @Test
    void createDiscount_multipleTargetingOptionsSelected_throwsInvalidTargeting() {
        DiscountCreateRequest request = requestWithTargeting(true, List.of(1L), null, null, null);

        assertThatThrownBy(() -> service().createDiscount(request))
                .isInstanceOfSatisfying(DiscountException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo("DISCOUNT_INVALID_TARGETING"));
        verifyNoInteractions(discountRepository);
    }

    @Test
    void createDiscount_variantTargetingWithExclusions_throwsInvalidExclusion() {
        DiscountCreateRequest request = requestWithTargeting(false, null, null, List.of(1L), List.of(2L));

        assertThatThrownBy(() -> service().createDiscount(request))
                .isInstanceOfSatisfying(DiscountException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo("DISCOUNT_INVALID_EXCLUSION"));
        verifyNoInteractions(discountRepository);
    }

    @Test
    void createDiscount_endDateBeforeStartDate_throwsInvalidDateRange() {
        DiscountCreateRequest request = new DiscountCreateRequest("Sale", DiscountType.PERCENTAGE, BigDecimal.TEN,
                LocalDateTime.now(), LocalDateTime.now().minusDays(1), null, null, null, null,
                true, null, null, null, null);

        assertThatThrownBy(() -> service().createDiscount(request))
                .isInstanceOfSatisfying(DiscountException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo("DISCOUNT_INVALID_DATE_RANGE"));
        verifyNoInteractions(discountRepository);
    }

    @Test
    void createDiscount_codeAlreadyTaken_throwsCodeAlreadyInUse() {
        DiscountCreateRequest request = new DiscountCreateRequest("Sale", DiscountType.PERCENTAGE, BigDecimal.TEN,
                LocalDateTime.now(), LocalDateTime.now().plusDays(7), "SAVE10", null, null, null,
                true, null, null, null, null);
        when(discountRepository.existsByCode("SAVE10")).thenReturn(true);

        assertThatThrownBy(() -> service().createDiscount(request))
                .isInstanceOfSatisfying(DiscountException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo("DISCOUNT_CODE_ALREADY_IN_USE"));
    }
}
