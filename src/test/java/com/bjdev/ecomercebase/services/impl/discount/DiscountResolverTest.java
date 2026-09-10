package com.bjdev.ecomercebase.services.impl.discount;

import com.bjdev.ecomercebase.exception.DiscountException;
import com.bjdev.ecomercebase.models.cart.CartLine;
import com.bjdev.ecomercebase.models.catalog.Category;
import com.bjdev.ecomercebase.models.catalog.Item;
import com.bjdev.ecomercebase.models.catalog.ItemVariant;
import com.bjdev.ecomercebase.models.discount.Discount;
import com.bjdev.ecomercebase.models.discount.DiscountCategory;
import com.bjdev.ecomercebase.models.discount.DiscountExcludedVariant;
import com.bjdev.ecomercebase.models.enums.DiscountTargetType;
import com.bjdev.ecomercebase.models.enums.DiscountType;
import com.bjdev.ecomercebase.repositories.discount.DiscountCategoryRepository;
import com.bjdev.ecomercebase.repositories.discount.DiscountExcludedVariantRepository;
import com.bjdev.ecomercebase.repositories.discount.DiscountItemRepository;
import com.bjdev.ecomercebase.repositories.discount.DiscountRedemptionRepository;
import com.bjdev.ecomercebase.repositories.discount.DiscountRepository;
import com.bjdev.ecomercebase.repositories.discount.DiscountVariantRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Covers the actual money math checkout relies on: how much a discount takes off, and the guardrails
 * (min purchase, usage caps, targeting/exclusions) around it. Pure Mockito unit tests — no database.
 */
@ExtendWith(MockitoExtension.class)
class DiscountResolverTest {

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
    private DiscountRedemptionRepository discountRedemptionRepository;

    private static final Long CLIENT_ID = 1L;

    private DiscountResolver resolver() {
        return new DiscountResolver(discountRepository, discountCategoryRepository, discountItemRepository,
                discountVariantRepository, discountExcludedVariantRepository, discountRedemptionRepository);
    }

    private Category category(Long id) {
        return Category.builder().id(id).name("Cat" + id).slug("cat-" + id).build();
    }

    private ItemVariant variant(Long id, Category category, BigDecimal price) {
        Item item = Item.builder().id(id).name("Item" + id).category(category).build();
        return ItemVariant.builder().id(id).item(item).sku("SKU" + id).name("Variant" + id).price(price).build();
    }

    private CartLine line(ItemVariant variant, int quantity, BigDecimal priceAtAddition) {
        return CartLine.builder().variant(variant).quantity(quantity).priceAtAddition(priceAtAddition).build();
    }

    private Discount discount(DiscountType type, BigDecimal value, DiscountTargetType targetType, String code) {
        return Discount.builder()
                .id(1L)
                .title("Test discount")
                .type(type)
                .value(value)
                .targetType(targetType)
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(1))
                .active(true)
                .code(code)
                .usedCount(0)
                .build();
    }

    @Test
    void resolve_percentageOnAllCatalog_appliesToWholeSubtotal() {
        Category cat = category(1L);
        ItemVariant v = variant(1L, cat, new BigDecimal("100.00"));
        List<CartLine> lines = List.of(line(v, 2, new BigDecimal("100.00"))); // subtotal 200

        Discount discount = discount(DiscountType.PERCENTAGE, new BigDecimal("10"), DiscountTargetType.ALL, "SAVE10");
        when(discountRepository.findByCodeAndActiveTrue("SAVE10")).thenReturn(Optional.of(discount));
        when(discountExcludedVariantRepository.findByDiscountId(1L)).thenReturn(List.of());

        Optional<DiscountApplication> result = resolver().resolve(lines, CLIENT_ID, "SAVE10");

        assertThat(result).isPresent();
        assertThat(result.get().amount()).isEqualByComparingTo("20.00"); // 10% of 200
    }

    @Test
    void resolve_fixedAmountLargerThanEligibleSubtotal_isCappedAtSubtotal() {
        Category cat = category(1L);
        ItemVariant v = variant(1L, cat, new BigDecimal("15.00"));
        List<CartLine> lines = List.of(line(v, 1, new BigDecimal("15.00"))); // subtotal 15

        Discount discount = discount(DiscountType.FIXED_AMOUNT, new BigDecimal("50.00"), DiscountTargetType.ALL, "BIG50");
        when(discountRepository.findByCodeAndActiveTrue("BIG50")).thenReturn(Optional.of(discount));
        when(discountExcludedVariantRepository.findByDiscountId(1L)).thenReturn(List.of());

        Optional<DiscountApplication> result = resolver().resolve(lines, CLIENT_ID, "BIG50");

        assertThat(result).isPresent();
        assertThat(result.get().amount()).isEqualByComparingTo("15.00"); // never more than the cart itself
    }

    @Test
    void resolve_categoryTargeting_onlyDiscountsMatchingLines() {
        Category electronics = category(1L);
        Category groceries = category(2L);
        ItemVariant laptop = variant(1L, electronics, new BigDecimal("1000.00"));
        ItemVariant bread = variant(2L, groceries, new BigDecimal("5.00"));
        List<CartLine> lines = List.of(line(laptop, 1, new BigDecimal("1000.00")), line(bread, 1, new BigDecimal("5.00")));

        Discount discount = discount(DiscountType.PERCENTAGE, new BigDecimal("10"), DiscountTargetType.CATEGORY, "ELEC10");
        when(discountRepository.findByCodeAndActiveTrue("ELEC10")).thenReturn(Optional.of(discount));
        when(discountExcludedVariantRepository.findByDiscountId(1L)).thenReturn(List.of());
        when(discountCategoryRepository.findByDiscountId(1L)).thenReturn(
                List.of(DiscountCategory.builder().discount(discount).category(electronics).build()));

        Optional<DiscountApplication> result = resolver().resolve(lines, CLIENT_ID, "ELEC10");

        assertThat(result).isPresent();
        assertThat(result.get().amount()).isEqualByComparingTo("100.00"); // 10% of the laptop only, not the bread
    }

    @Test
    void resolve_excludedVariant_isNeverDiscountedEvenUnderAllTargeting() {
        Category cat = category(1L);
        ItemVariant excluded = variant(1L, cat, new BigDecimal("100.00"));
        ItemVariant regular = variant(2L, cat, new BigDecimal("50.00"));
        List<CartLine> lines = List.of(line(excluded, 1, new BigDecimal("100.00")), line(regular, 1, new BigDecimal("50.00")));

        Discount discount = discount(DiscountType.PERCENTAGE, new BigDecimal("10"), DiscountTargetType.ALL, "SAVE10");
        when(discountRepository.findByCodeAndActiveTrue("SAVE10")).thenReturn(Optional.of(discount));
        when(discountExcludedVariantRepository.findByDiscountId(1L)).thenReturn(
                List.of(DiscountExcludedVariant.builder().discount(discount).variant(excluded).build()));

        Optional<DiscountApplication> result = resolver().resolve(lines, CLIENT_ID, "SAVE10");

        assertThat(result).isPresent();
        assertThat(result.get().amount()).isEqualByComparingTo("5.00"); // 10% of the regular item only
    }

    @Test
    void resolve_belowMinPurchase_throwsForAnExplicitCode() {
        Category cat = category(1L);
        ItemVariant v = variant(1L, cat, new BigDecimal("10.00"));
        List<CartLine> lines = List.of(line(v, 1, new BigDecimal("10.00")));

        Discount discount = discount(DiscountType.PERCENTAGE, new BigDecimal("10"), DiscountTargetType.ALL, "SAVE10");
        discount.setMinPurchaseAmount(new BigDecimal("50.00"));
        when(discountRepository.findByCodeAndActiveTrue("SAVE10")).thenReturn(Optional.of(discount));

        assertThatThrownBy(() -> resolver().resolve(lines, CLIENT_ID, "SAVE10"))
                .isInstanceOfSatisfying(DiscountException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo("DISCOUNT_MIN_PURCHASE_NOT_MET"));
    }

    @Test
    void resolve_unknownCode_throwsInvalidCode() {
        when(discountRepository.findByCodeAndActiveTrue("NOPE")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> resolver().resolve(List.of(), CLIENT_ID, "NOPE"))
                .isInstanceOfSatisfying(DiscountException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo("DISCOUNT_CODE_INVALID"));
    }

    @Test
    void resolve_usageLimitAlreadyReached_throwsForAnExplicitCode() {
        Category cat = category(1L);
        ItemVariant v = variant(1L, cat, new BigDecimal("10.00"));
        List<CartLine> lines = List.of(line(v, 1, new BigDecimal("10.00")));

        Discount discount = discount(DiscountType.PERCENTAGE, new BigDecimal("10"), DiscountTargetType.ALL, "SAVE10");
        discount.setMaxUsesTotal(5);
        discount.setUsedCount(5);
        when(discountRepository.findByCodeAndActiveTrue("SAVE10")).thenReturn(Optional.of(discount));

        assertThatThrownBy(() -> resolver().resolve(lines, CLIENT_ID, "SAVE10"))
                .isInstanceOfSatisfying(DiscountException.class,
                        e -> assertThat(e.getErrorCode()).isEqualTo("DISCOUNT_USAGE_LIMIT_REACHED"));
    }

    @Test
    void resolve_noCodeGiven_picksTheBestOfMultipleEligibleAutoApplyDiscounts() {
        Category cat = category(1L);
        ItemVariant v = variant(1L, cat, new BigDecimal("100.00"));
        List<CartLine> lines = List.of(line(v, 1, new BigDecimal("100.00")));

        Discount small = discount(DiscountType.PERCENTAGE, new BigDecimal("5"), DiscountTargetType.ALL, null);
        small.setId(1L);
        Discount big = discount(DiscountType.PERCENTAGE, new BigDecimal("20"), DiscountTargetType.ALL, null);
        big.setId(2L);

        when(discountRepository.findAutoApplyCandidates(org.mockito.ArgumentMatchers.any())).thenReturn(List.of(small, big));
        when(discountExcludedVariantRepository.findByDiscountId(1L)).thenReturn(List.of());
        when(discountExcludedVariantRepository.findByDiscountId(2L)).thenReturn(List.of());

        Optional<DiscountApplication> result = resolver().resolve(lines, CLIENT_ID, null);

        assertThat(result).isPresent();
        assertThat(result.get().discount().getId()).isEqualTo(2L);
        assertThat(result.get().amount()).isEqualByComparingTo("20.00");
    }

    @Test
    void resolve_noCodeAndNoEligibleAutoApplyDiscount_returnsEmpty() {
        when(discountRepository.findAutoApplyCandidates(org.mockito.ArgumentMatchers.any())).thenReturn(List.of());

        Optional<DiscountApplication> result = resolver().resolve(List.of(), CLIENT_ID, null);

        assertThat(result).isEmpty();
    }
}
