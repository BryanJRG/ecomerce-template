package com.bjdev.ecomercebase.models.refund;

import com.bjdev.ecomercebase.models.order.OrderLine;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "refund_lines", indexes = {
        @Index(name = "idx_refund_line_refund", columnList = "refund_id"),
        @Index(name = "idx_refund_line_order_line", columnList = "order_line_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefundLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "refund_id", nullable = false)
    private Refund refund;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_line_id", nullable = false)
    private OrderLine orderLine;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;
}
