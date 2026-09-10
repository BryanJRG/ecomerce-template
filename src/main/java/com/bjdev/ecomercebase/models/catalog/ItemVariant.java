package com.bjdev.ecomercebase.models.catalog;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Item is never hard-deleted, and this variant's FK to it uses the database's default
 * ON DELETE RESTRICT (no cascade) — so a delete attempt fails loudly instead of orphaning rows.
 */
@Entity
@Table(name = "item_variants", indexes = {
        @Index(name = "idx_variant_item", columnList = "item_id"),
        @Index(name = "idx_variant_sku", columnList = "sku", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @Column(nullable = false, unique = true, length = 64)
    private String sku;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    @Builder.Default
    private Integer stock = 0;

    @Column(name = "low_stock_threshold", nullable = false)
    @Builder.Default
    private Integer lowStockThreshold = 5;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    /** Optimistic lock guarding concurrent stock decrements (e.g. simultaneous checkouts). */
    @Version
    @Column(nullable = false)
    private Long version;
}
