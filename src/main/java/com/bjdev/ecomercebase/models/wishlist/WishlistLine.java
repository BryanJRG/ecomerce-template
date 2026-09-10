package com.bjdev.ecomercebase.models.wishlist;

import com.bjdev.ecomercebase.models.catalog.ItemVariant;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "wishlist_lines", indexes = {
        @Index(name = "idx_wishlist_line_wishlist", columnList = "wishlist_id"),
        @Index(name = "idx_wishlist_line_variant", columnList = "variant_id")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_wishlist_variant", columnNames = {"wishlist_id", "variant_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WishlistLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "wishlist_id", nullable = false)
    private Wishlist wishlist;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "variant_id", nullable = false)
    private ItemVariant variant;

    @CreationTimestamp
    @Column(name = "added_at", nullable = false, updatable = false)
    private LocalDateTime addedAt;
}
