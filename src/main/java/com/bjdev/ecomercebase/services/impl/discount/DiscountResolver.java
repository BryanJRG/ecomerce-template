package com.bjdev.ecomercebase.services.impl.discount;

import com.bjdev.ecomercebase.exception.DiscountException;
import com.bjdev.ecomercebase.models.cart.CartLine;
import com.bjdev.ecomercebase.models.catalog.ItemVariant;
import com.bjdev.ecomercebase.models.client.Client;
import com.bjdev.ecomercebase.models.discount.Discount;
import com.bjdev.ecomercebase.models.discount.DiscountRedemption;
import com.bjdev.ecomercebase.models.enums.DiscountTargetType;
import com.bjdev.ecomercebase.models.order.Order;
import com.bjdev.ecomercebase.repositories.discount.DiscountCategoryRepository;
import com.bjdev.ecomercebase.repositories.discount.DiscountExcludedVariantRepository;
import com.bjdev.ecomercebase.repositories.discount.DiscountItemRepository;
import com.bjdev.ecomercebase.repositories.discount.DiscountRedemptionRepository;
import com.bjdev.ecomercebase.repositories.discount.DiscountRepository;
import com.bjdev.ecomercebase.repositories.discount.DiscountVariantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Turns the catalog-facing Discount configuration (targeting, exclusions, usage caps) into an
 * actual amount off a cart at checkout — the piece that was missing between DiscountService
 * (admin CRUD) and CheckoutServiceImpl (which used to compute totals with no discount at all).
 * <p>
 * At most one discount applies per order — an explicit code if the customer entered one, otherwise
 * the best (highest-amount) currently-eligible auto-apply discount. Stacking multiple discounts is
 * out of scope; extend {@link #resolve} if a future project needs it.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DiscountResolver {

    private final DiscountRepository discountRepository;
    private final DiscountCategoryRepository discountCategoryRepository;
    private final DiscountItemRepository discountItemRepository;
    private final DiscountVariantRepository discountVariantRepository;
    private final DiscountExcludedVariantRepository discountExcludedVariantRepository;
    private final DiscountRedemptionRepository discountRedemptionRepository;

    /**
     * Read-only but still transactional: eligibility needs variant -> item -> category, which are
     * lazy associations that must be resolved inside an active transaction rather than relying on
     * open-in-view (CheckoutServiceImpl deliberately avoids depending on that — see its class doc).
     */
    @Transactional(readOnly = true)
    public Optional<DiscountApplication> resolve(List<CartLine> lines, Long clientId, String code) {
        BigDecimal subtotal = subtotalOf(lines);
        LocalDateTime now = LocalDateTime.now();

        if (code != null && !code.isBlank()) {
            Discount discount = discountRepository.findByCodeAndActiveTrue(code)
                    .filter(d -> !now.isBefore(d.getStartDate()) && !now.isAfter(d.getEndDate()))
                    .orElseThrow(DiscountException::invalidCode);
            validateEligibleOrThrow(discount, subtotal, clientId);

            BigDecimal amount = computeAmount(discount, lines);
            if (amount.signum() <= 0) {
                throw DiscountException.notApplicable();
            }
            return Optional.of(new DiscountApplication(discount, amount));
        }

        return discountRepository.findAutoApplyCandidates(now).stream()
                .filter(d -> isEligible(d, subtotal, clientId))
                .map(d -> new DiscountApplication(d, computeAmount(d, lines)))
                .filter(a -> a.amount().signum() > 0)
                .max(Comparator.comparing(DiscountApplication::amount));
    }

    /**
     * Records the redemption and atomically enforces maxUsesTotal — called only after payment is
     * approved, from within the same transaction as order creation (see CheckoutOrderFinalizer).
     * If the atomic cap check loses a race at this late point (another checkout claimed the last
     * use between resolve() and here), the order still completes — the customer was already
     * charged the discounted amount, so clawing it back after a successful payment isn't reasonable.
     */
    @Transactional
    public void redeem(DiscountApplication application, Client client, Order order) {
        Discount discount = application.discount();
        if (discountRepository.redeem(discount.getId()) == 0) {
            log.warn("Discount {} hit its usage cap between checkout and order finalization — order {} "
                    + "still completes without a recorded redemption.", discount.getId(), order.getId());
            return;
        }
        discountRedemptionRepository.save(DiscountRedemption.builder()
                .discount(discount)
                .client(client)
                .order(order)
                .amount(application.amount())
                .build());
    }

    private void validateEligibleOrThrow(Discount discount, BigDecimal subtotal, Long clientId) {
        if (discount.getMinPurchaseAmount() != null && subtotal.compareTo(discount.getMinPurchaseAmount()) < 0) {
            throw DiscountException.minPurchaseNotMet(discount.getMinPurchaseAmount());
        }
        if (exceedsUsageCap(discount, clientId)) {
            throw DiscountException.usageLimitReached();
        }
    }

    private boolean isEligible(Discount discount, BigDecimal subtotal, Long clientId) {
        if (discount.getMinPurchaseAmount() != null && subtotal.compareTo(discount.getMinPurchaseAmount()) < 0) {
            return false;
        }
        return !exceedsUsageCap(discount, clientId);
    }

    private boolean exceedsUsageCap(Discount discount, Long clientId) {
        if (discount.getMaxUsesTotal() != null && discount.getUsedCount() >= discount.getMaxUsesTotal()) {
            return true;
        }
        return discount.getMaxUsesPerClient() != null
                && discountRedemptionRepository.countByDiscountIdAndClientId(discount.getId(), clientId) >= discount.getMaxUsesPerClient();
    }

    private BigDecimal computeAmount(Discount discount, List<CartLine> lines) {
        BigDecimal eligible = eligibleSubtotal(discount, lines);
        if (eligible.signum() <= 0) {
            return BigDecimal.ZERO;
        }
        return switch (discount.getType()) {
            case PERCENTAGE -> eligible.multiply(discount.getValue())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            case FIXED_AMOUNT -> discount.getValue().min(eligible);
        };
    }

    /** Sum of cart lines that fall under this discount's targeting and aren't in its exclusion set. */
    private BigDecimal eligibleSubtotal(Discount discount, List<CartLine> lines) {
        Set<Long> excludedVariantIds = discount.getTargetType() == DiscountTargetType.VARIANT
                ? Set.of()
                : discountExcludedVariantRepository.findByDiscountId(discount.getId()).stream()
                        .map(e -> e.getVariant().getId()).collect(Collectors.toSet());

        Set<Long> targetIds = switch (discount.getTargetType()) {
            case CATEGORY -> discountCategoryRepository.findByDiscountId(discount.getId()).stream()
                    .map(c -> c.getCategory().getId()).collect(Collectors.toSet());
            case ITEM -> discountItemRepository.findByDiscountId(discount.getId()).stream()
                    .map(i -> i.getItem().getId()).collect(Collectors.toSet());
            case VARIANT -> discountVariantRepository.findByDiscountId(discount.getId()).stream()
                    .map(v -> v.getVariant().getId()).collect(Collectors.toSet());
            case ALL -> Set.of();
        };

        BigDecimal total = BigDecimal.ZERO;
        for (CartLine line : lines) {
            ItemVariant variant = line.getVariant();
            if (excludedVariantIds.contains(variant.getId())) {
                continue;
            }
            boolean matches = switch (discount.getTargetType()) {
                case ALL -> true;
                case CATEGORY -> targetIds.contains(variant.getItem().getCategory().getId());
                case ITEM -> targetIds.contains(variant.getItem().getId());
                case VARIANT -> targetIds.contains(variant.getId());
            };
            if (matches) {
                total = total.add(line.getPriceAtAddition().multiply(BigDecimal.valueOf(line.getQuantity())));
            }
        }
        return total;
    }

    private BigDecimal subtotalOf(List<CartLine> lines) {
        return lines.stream()
                .map(l -> l.getPriceAtAddition().multiply(BigDecimal.valueOf(l.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
