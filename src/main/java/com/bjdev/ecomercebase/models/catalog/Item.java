package com.bjdev.ecomercebase.models.catalog;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Never hard-deleted — use {@link #active} to remove an item from the catalog. No @OneToMany
 * here by design: variants/images are read via repository, never navigated from this entity.
 */
@Entity
@Table(name = "items", indexes = {
        @Index(name = "idx_item_category", columnList = "category_id"),
        @Index(name = "idx_item_brand", columnList = "brand_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    private Brand brand;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    /** Denormalized sum of active variants' stock — kept in sync by the service layer on stock changes. */
    @Column(name = "total_stock", nullable = false)
    @Builder.Default
    private Integer totalStock = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
