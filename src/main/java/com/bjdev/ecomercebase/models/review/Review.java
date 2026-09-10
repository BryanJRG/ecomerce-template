package com.bjdev.ecomercebase.models.review;

import com.bjdev.ecomercebase.models.catalog.Item;
import com.bjdev.ecomercebase.models.order.OrderLine;
import com.bjdev.ecomercebase.models.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Ties to {@link User} rather than Client: posting a review is an authenticated action taken
 * well after checkout (from a returning session), unlike Cart/Order which must also support the
 * guest-checkout moment itself. orderLine (optional) links the specific purchase that verifies it.
 */
@Entity
@Table(name = "reviews", indexes = {
        @Index(name = "idx_review_item", columnList = "item_id"),
        @Index(name = "idx_review_user", columnList = "user_id")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_review_user_item", columnNames = {"user_id", "item_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    /** Set when this review is tied to a verified purchase; null for unverified reviews. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_line_id")
    private OrderLine orderLine;

    @Column(nullable = false)
    private Integer rating;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
