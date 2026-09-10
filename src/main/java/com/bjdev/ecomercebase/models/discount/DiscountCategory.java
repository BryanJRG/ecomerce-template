package com.bjdev.ecomercebase.models.discount;

import com.bjdev.ecomercebase.models.catalog.Category;
import jakarta.persistence.*;
import lombok.*;

/**
 * Populated only when {@link Discount#getTargetType()} is {@code CATEGORY}. See {@link Discount}'s
 * class doc for the targeting design rationale.
 */
@Entity
@Table(name = "discount_categories", indexes = {
        @Index(name = "idx_discount_category_category", columnList = "category_id")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_discount_category", columnNames = {"discount_id", "category_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiscountCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "discount_id", nullable = false)
    private Discount discount;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
}
