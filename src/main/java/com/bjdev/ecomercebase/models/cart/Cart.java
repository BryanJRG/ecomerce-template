package com.bjdev.ecomercebase.models.cart;

import com.bjdev.ecomercebase.models.client.Client;
import com.bjdev.ecomercebase.models.enums.CartStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Persisted only once a {@link Client} exists (registered login/registration, or at guest
 * checkout) — carts for anonymous visitors live in the frontend (localStorage), never here.
 * No @OneToMany here by design: lines are read via repository (findByCartId), never navigated
 * from this entity.
 */
@Entity
@Table(name = "carts", indexes = {
        @Index(name = "idx_cart_client", columnList = "client_id"),
        @Index(name = "idx_cart_status_updated", columnList = "status, updated_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private CartStatus status = CartStatus.ACTIVE;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Drives the monthly hard-delete job for abandoned carts — see CartServiceImpl. */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
