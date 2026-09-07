package com.bjdev.base.models.notification;

import com.bjdev.base.models.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * A single in-app notification delivered to one recipient.
 * Role-wide notifications are fanned out to one row per user at write time (see NotificationService),
 * which keeps reads (unread count, listing, marking as read) simple per-user queries.
 */
@Entity
@Table(name = "notifications", indexes = {
        @Index(name = "idx_notification_recipient", columnList = "recipient_id"),
        @Index(name = "idx_notification_recipient_read", columnList = "recipient_id, is_read"),
        @Index(name = "idx_notification_type", columnList = "type"),
        @Index(name = "idx_notification_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Who this notification is for. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    /** Who triggered it, if any (null for system-generated notifications). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id")
    private User actor;

    /** Namespaced type key, e.g. "admin_invitation.created" — a String on purpose so new
     *  modules/variants can introduce their own notification types without touching a shared enum. */
    @Column(nullable = false, length = 100)
    private String type;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String message;

    /** Optional link back to the record this notification is about (mirrors AuditLog's convention). */
    @Column(name = "related_table", length = 100)
    private String relatedTable;

    @Column(name = "related_id")
    private Long relatedId;

    /** Structured extra payload (e.g. ids/links) for clients that want more than the text. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "JSON")
    @Builder.Default
    private Map<String, Object> data = new HashMap<>();

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean isRead = false;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
