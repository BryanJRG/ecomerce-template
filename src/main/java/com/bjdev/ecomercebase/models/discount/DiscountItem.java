package com.bjdev.ecomercebase.models.discount;

import com.bjdev.ecomercebase.models.catalog.Item;
import jakarta.persistence.*;
import lombok.*;

/**
 * Populated only when {@link Discount#getTargetType()} is {@code ITEM}. See {@link Discount}'s
 * class doc for the targeting design rationale.
 */
@Entity
@Table(name = "discount_items", indexes = {
        @Index(name = "idx_discount_item_item", columnList = "item_id")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_discount_item", columnNames = {"discount_id", "item_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiscountItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "discount_id", nullable = false)
    private Discount discount;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;
}
