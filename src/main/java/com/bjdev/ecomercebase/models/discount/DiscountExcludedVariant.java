package com.bjdev.ecomercebase.models.discount;

import com.bjdev.ecomercebase.models.catalog.ItemVariant;
import jakarta.persistence.*;
import lombok.*;

/**
 * Universal exclusion table, applicable when {@link Discount#getTargetType()} is {@code ALL},
 * {@code CATEGORY} or {@code ITEM} — never {@code VARIANT}, because there inclusion is already
 * explicit ({@link DiscountVariant}) and an exclusion would be ambiguous/contradictory. That rule
 * is validated in the service layer, not here.
 */
@Entity
@Table(name = "discount_excluded_variants", indexes = {
        @Index(name = "idx_discount_excluded_variant_variant", columnList = "variant_id")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_discount_excluded_variant", columnNames = {"discount_id", "variant_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiscountExcludedVariant {

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
