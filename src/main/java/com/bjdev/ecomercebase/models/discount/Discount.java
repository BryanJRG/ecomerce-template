package com.bjdev.ecomercebase.models.discount;

import com.bjdev.ecomercebase.models.enums.DiscountTargetType;
import com.bjdev.ecomercebase.models.enums.DiscountType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Targeting is deliberately NOT modeled as combinable boolean flags (e.g. "applyToAll",
 * "applyExceptX") persisted as-is — evaluating that at every price lookup would require a chain of
 * conditionals instead of an indexed query. Instead, the admin-facing DTO
 * ({@code DiscountCreateRequest}) accepts the flexibility admins need (toggles, id lists), and the
 * service layer (a later task) translates that into this single canonical {@link #targetType} plus,
 * optionally, a universal exclusion table ({@link DiscountExcludedVariant}) — so resolving "does
 * this discount apply to this variant" is always an indexed lookup against one of
 * {@link DiscountCategory}/{@link DiscountItem}/{@link DiscountVariant}, regardless of how flexible
 * the admin's input was. No @OneToMany to any targeting/redemption table by design, same reasoning
 * as {@link com.bjdev.ecomercebase.models.catalog.Item}: they're read via repository, never
 * navigated from here.
 */
@Entity
@Table(name = "discounts", indexes = {
        @Index(name = "idx_discount_code", columnList = "code", unique = true),
        @Index(name = "idx_discount_active_dates", columnList = "active, start_date, end_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Discount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DiscountType type;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal value;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 20)
    private DiscountTargetType targetType;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    /** Null means the discount applies automatically to whatever qualifies; non-null requires the client to enter it at checkout. */
    @Column(length = 50, unique = true)
    private String code;

    @Column(name = "min_purchase_amount", precision = 12, scale = 2)
    private BigDecimal minPurchaseAmount;

    @Column(name = "max_uses_total")
    private Integer maxUsesTotal;

    @Column(name = "max_uses_per_client")
    private Integer maxUsesPerClient;

    /** Atomically incremented by DiscountRepository.redeem — the same conditional-UPDATE idiom as ItemVariantRepository.reserveStock — never written to directly. */
    @Column(name = "used_count", nullable = false)
    @Builder.Default
    private Integer usedCount = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
