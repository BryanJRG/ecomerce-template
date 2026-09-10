package com.bjdev.ecomercebase.models.wishlist;

import com.bjdev.ecomercebase.models.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Account-only feature with no guest equivalent, so this ties directly to {@link User} rather
 * than Client; an anonymous visitor's favorites live in the frontend until they register/log in.
 * No @OneToMany here by design: lines are read via repository (findByWishlistId).
 */
@Entity
@Table(name = "wishlists", indexes = {
        @Index(name = "idx_wishlist_user", columnList = "user_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Wishlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
