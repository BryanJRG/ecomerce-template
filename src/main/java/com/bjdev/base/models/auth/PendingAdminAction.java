package com.bjdev.base.models.auth;

import com.bjdev.base.models.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * A critical admin action awaiting confirmation via a token emailed to the admin who requested it
 * (see AdminActionConfirmationService). This proves the requester still controls their own email
 * account, independently of whatever JWT session initiated the request — the point of the gate is
 * to survive a stolen/replayed admin token.
 */
@Entity
@Table(name = "pending_admin_actions", indexes = {
        @Index(name = "idx_pending_action_token", columnList = "token_hash", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PendingAdminAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private PendingActionType type;

    /** Minimal single-value payload for the action (e.g. the invited email, or a target user id). */
    @Column(nullable = false, length = 255)
    private String payload;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requested_by_user_id", nullable = false)
    private User requestedBy;

    @Column(name = "token_hash", nullable = false, unique = true)
    private String tokenHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private PendingActionStatus status = PendingActionStatus.PENDING;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
