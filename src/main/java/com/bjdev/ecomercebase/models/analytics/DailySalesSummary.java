package com.bjdev.ecomercebase.models.analytics;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * One row per calendar day, updated incrementally as orders are paid (see SalesSummaryCoordinator)
 * rather than computed on read — the admin dashboard never has to aggregate the full orders table.
 * Monthly/quarterly/yearly views are SUM/GROUP BY (or, given how small this table stays, an
 * in-memory grouping) over these rows — never separate rollup tables, to avoid staleness between
 * granularities.
 */
@Entity
@Table(name = "daily_sales_summaries", uniqueConstraints = {
        @UniqueConstraint(name = "uk_daily_sales_summary_date", columnNames = "sales_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailySalesSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sales_date", nullable = false, updatable = false)
    private LocalDate salesDate;

    @Column(name = "order_count", nullable = false)
    @Builder.Default
    private Integer orderCount = 0;

    @Column(name = "gross_revenue", nullable = false, precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal grossRevenue = BigDecimal.ZERO;
}
