package com.bjdev.ecomercebase.models.checkout;

import com.bjdev.ecomercebase.models.cart.Cart;
import com.bjdev.ecomercebase.models.catalog.ItemVariant;
import com.bjdev.ecomercebase.models.enums.ReservationStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * A durable trace of a stock decrement made at checkout time, kept independent of {@link
 * com.bjdev.ecomercebase.models.payment.Payment} on purpose: stock is reserved BEFORE the payment
 * provider is called (see CheckoutServiceImpl), and {@code Payment.order} is non-null, but no
 * Order exists yet at that point — there would be nowhere to record an in-flight reservation.
 * This row is what the orphaned-reservation cleanup job scans: any row still PENDING after the
 * configured timeout means the provider (e.g. a Wompi 3DS challenge) never came back, and its
 * stock must be released back to the pool.
 */
@Entity
@Table(name = "stock_reservations", indexes = {
        @Index(name = "idx_reservation_status_reserved_at", columnList = "status, reserved_at"),
        @Index(name = "idx_reservation_variant", columnList = "variant_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "variant_id", nullable = false)
    private ItemVariant variant;

    @Column(nullable = false)
    private Integer quantity;

    /** The cart this reservation was made for — kept so a failed checkout can be traced back to it. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @Column(name = "idempotency_key", length = 100)
    private String idempotencyKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ReservationStatus status = ReservationStatus.PENDING;

    @CreationTimestamp
    @Column(name = "reserved_at", nullable = false, updatable = false)
    private LocalDateTime reservedAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;
}
