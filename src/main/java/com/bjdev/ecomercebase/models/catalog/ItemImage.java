package com.bjdev.ecomercebase.models.catalog;

import jakarta.persistence.*;
import lombok.*;

/**
 * Historical/dependent record: FKs to Item/ItemVariant use the database's default
 * ON DELETE RESTRICT (no cascade), so neither can be hard-deleted while images reference them.
 */
@Entity
@Table(name = "item_images", indexes = {
        @Index(name = "idx_image_item", columnList = "item_id"),
        @Index(name = "idx_image_variant", columnList = "variant_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    /** Set when this image belongs to a specific variant (e.g. a color); null for item-level images. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id")
    private ItemVariant variant;

    @Column(nullable = false, length = 500)
    private String url;

    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private Integer displayOrder = 0;

    @Column(name = "is_primary", nullable = false)
    @Builder.Default
    private Boolean isPrimary = false;
}
