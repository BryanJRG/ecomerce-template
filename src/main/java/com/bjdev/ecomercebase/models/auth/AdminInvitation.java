package com.bjdev.ecomercebase.models.auth;

import com.bjdev.ecomercebase.models.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "admin_invitations", indexes = {
        @Index(name = "idx_invitation_token", columnList = "token_hash", unique = true),
        @Index(name = "idx_invitation_email", columnList = "invited_email")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminInvitation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** SHA-256 hash of the UUID token sent in the invitation email link — the raw token is never stored. */
    @Column(name = "token_hash", nullable = false, unique = true)
    private String tokenHash;

    /** The email address that is allowed to accept this invitation. */
    @Column(name = "invited_email", nullable = false, length = 255)
    private String invitedEmail;

    /** The role to grant on acceptance (e.g. "ADMIN"). */
    @Column(name = "role_to_grant", nullable = false, length = 50)
    private String roleToGrant;

    /** The admin user who created this invitation. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private User createdBy;

    /** When this token expires (default 24 hours from creation). */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /** Current lifecycle state of the invitation. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private InvitationStatus status = InvitationStatus.PENDING;

    /** When the invitation was accepted/rejected/cancelled (null while pending). */
    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    /** The user who accepted the invitation (null unless accepted). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accepted_by_user_id")
    private User acceptedBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}