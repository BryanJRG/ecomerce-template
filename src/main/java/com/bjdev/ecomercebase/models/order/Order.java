package com.bjdev.ecomercebase.models.order;

import com.bjdev.ecomercebase.models.address.ShippingAddress;
import com.bjdev.ecomercebase.models.client.Client;
import com.bjdev.ecomercebase.models.enums.OrderStatus;
import com.bjdev.ecomercebase.models.enums.SalesChannel;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * No @OneToMany here by design: lines/status history are read via repository
 * (findByOrderId), never navigated from this entity — updating just the status must not
 * risk dragging the whole line collection along.
 */
@Entity
@Table(name = "orders", indexes = {
        @Index(name = "idx_order_client", columnList = "client_id"),
        @Index(name = "idx_order_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    /** The address this order ships to; captured at checkout time, guest or registered. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipping_address_id")
    private ShippingAddress shippingAddress;

    @Column(name = "tracking_number", length = 100)
    private String trackingNumber;

    /** Reporting only — stock is a single pool and is never partitioned by channel. */
    @Enumerated(EnumType.STRING)
    @Column(name = "sales_channel", nullable = false, length = 20)
    @Builder.Default
    private SalesChannel salesChannel = SalesChannel.ONLINE;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
