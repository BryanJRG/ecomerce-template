package com.bjdev.ecomercebase.models.discount;

import com.bjdev.ecomercebase.models.catalog.ItemVariant;
import jakarta.persistence.*;
import lombok.*;

/**
 * Populated only when {@link Discount#getTargetType()} is {@code VARIANT} — explicit inclusion, the
 * finest targeting granularity. See {@link Discount}'s class doc for the targeting design
 * rationale; see {@link DiscountExcludedVariant} for why exclusions never apply alongside this.
 */
@Entity
@Table(name = "discount_variants", indexes = {
        @Index(name = "idx_discount_variant_variant", columnList = "variant_id")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_discount_variant", columnNames = {"discount_id", "variant_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiscountVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "discount_id", nullable = false)
    private Discount discount;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "variant_id", nullable = false)
    private ItemVariant variant;
}
