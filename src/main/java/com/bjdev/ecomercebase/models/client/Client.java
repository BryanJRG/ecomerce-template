package com.bjdev.ecomercebase.models.client;

import com.bjdev.ecomercebase.models.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Commerce identity for anyone who places an order, with or without a registered {@link User}.
 * Created lazily — on registration/login (linked to a User) or at the moment a guest completes
 * checkout — never for anonymous browsing, to avoid persisting rows for visitors who never buy.
 * No @OneToMany here by design: carts/orders/addresses are read via repository, never navigated
 * from this entity, so loading a Client never risks dragging its purchase history along.
 */
@Entity
@Table(name = "clients", indexes = {
        @Index(name = "idx_client_email", columnList = "email", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Present only when this client has a registered account; null for guest checkouts. */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(length = 30)
    private String phone;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
