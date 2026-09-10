package com.bjdev.ecomercebase.models.cart;

import com.bjdev.ecomercebase.models.catalog.ItemVariant;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "cart_lines", indexes = {
        @Index(name = "idx_cart_line_cart", columnList = "cart_id"),
        @Index(name = "idx_cart_line_variant", columnList = "variant_id")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_cart_variant", columnNames = {"cart_id", "variant_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "variant_id", nullable = false)
    private ItemVariant variant;

    @Column(nullable = false)
    private Integer quantity;

    /** Unit price snapshot at the time this line was added, so later price changes don't retroactively alter the cart. */
    @Column(name = "price_at_addition", nullable = false, precision = 12, scale = 2)
    private BigDecimal priceAtAddition;
}
