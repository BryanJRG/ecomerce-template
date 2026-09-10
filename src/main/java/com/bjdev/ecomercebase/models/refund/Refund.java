package com.bjdev.ecomercebase.models.refund;

import com.bjdev.ecomercebase.models.enums.RefundStatus;
import com.bjdev.ecomercebase.models.order.Order;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * No @OneToMany here by design: lines/status history are read via repository
 * (findByRefundId), never navigated from this entity.
 */
@Entity
@Table(name = "refunds", indexes = {
        @Index(name = "idx_refund_order", columnList = "order_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Refund {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private RefundStatus status = RefundStatus.REQUESTED;

    @Column(nullable = false, length = 500)
    private String reason;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
