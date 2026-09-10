package com.bjdev.ecomercebase.models.order;

import com.bjdev.ecomercebase.models.catalog.ItemVariant;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Historical record: the FK to ItemVariant uses the database's default ON DELETE RESTRICT
 * (no cascade), so a variant with order lines cannot be hard-deleted — it must be soft-deleted
 * via its `active` flag instead.
 */
@Entity
@Table(name = "order_lines", indexes = {
        @Index(name = "idx_order_line_order", columnList = "order_id"),
        @Index(name = "idx_order_line_variant", columnList = "variant_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "variant_id", nullable = false)
    private ItemVariant variant;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "price_at_purchase", nullable = false, precision = 12, scale = 2)
    private BigDecimal priceAtPurchase;

    /** Item name at purchase time, so historical orders read correctly even if the item is later renamed. */
    @Column(name = "item_name_snapshot", nullable = false, length = 200)
    private String itemNameSnapshot;
}
