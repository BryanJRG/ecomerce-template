package com.bjdev.ecomercebase.models.review;

import com.bjdev.ecomercebase.models.user.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "review_votes", indexes = {
        @Index(name = "idx_review_vote_review", columnList = "review_id")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_review_vote_review_user", columnNames = {"review_id", "user_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewVote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Boolean helpful;
}
