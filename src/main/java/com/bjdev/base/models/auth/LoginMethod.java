package com.bjdev.base.models.auth;

import com.bjdev.base.models.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "login_method")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginMethod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AuthProvider provider;

    @Column(name = "provider_user_id")
    private String providerUserId; // null when local

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
