package com.bjdev.ecomercebase.models.discount;

import com.bjdev.ecomercebase.models.client.Client;
import com.bjdev.ecomercebase.models.order.Order;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Durable trace of a discount applied to an order. {@code unique(discount_id, order_id)} prevents
 * the same discount from being applied twice to the same order. In a later task this table backs
 * the atomic {@code maxUsesTotal}/{@code maxUsesPerClient} validation (COUNT under lock or a
 * conditional UPDATE, same pattern as {@code ItemVariantRepository.reserveStock}).
 */
@Entity
@Table(name = "discount_redemptions", indexes = {
        @Index(name = "idx_discount_redemption_discount_client", columnList = "discount_id, client_id")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_discount_redemption_order", columnNames = {"discount_id", "order_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiscountRedemption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "discount_id", nullable = false)
    private Discount discount;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    /** How much this redemption actually took off — a snapshot, same reasoning as OrderLine.priceAtPurchase: the discount's own value/targeting can change later without rewriting history. */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @CreationTimestamp
    @Column(name = "redeemed_at", nullable = false, updatable = false)
    private LocalDateTime redeemedAt;
}
