package com.bjdev.ecomercebase.models.catalog;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/** Never hard-deleted — use {@link #active} to remove a category from the catalog. */
@Entity
@Table(name = "categories", indexes = {
        @Index(name = "idx_category_slug", columnList = "slug", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, unique = true, length = 160)
    private String slug;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    /** Admin-configured price bounds for items in this category; null means no bound on that side. */
    @Column(name = "min_price", precision = 12, scale = 2)
    private BigDecimal minPrice;

    @Column(name = "max_price", precision = 12, scale = 2)
    private BigDecimal maxPrice;
}
