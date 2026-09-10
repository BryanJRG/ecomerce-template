package com.bjdev.ecomercebase.models.user;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * General audit log for system actions
 */
@Entity
@Table(name = "audit_logs", indexes = {
        @Index(name = "idx_user", columnList = "user_id"),
        @Index(name = "idx_table", columnList = "table_name"),
        @Index(name = "idx_action", columnList = "action"),
        @Index(name = "idx_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, length = 100)
    private String action;

    @Column(name = "table_name", nullable = false, length = 100)
    private String tableName;

    @Column(name = "record_id")
    private Long recordId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "old_values", columnDefinition = "JSON")
    @Builder.Default
    private Map<String, Object> oldValues = new HashMap<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "new_values", columnDefinition = "JSON")
    @Builder.Default
    private Map<String, Object> newValues = new HashMap<>();

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
