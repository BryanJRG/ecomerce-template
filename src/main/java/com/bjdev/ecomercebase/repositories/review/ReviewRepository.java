package com.bjdev.ecomercebase.repositories.review;

import com.bjdev.ecomercebase.models.review.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByItemId(Long itemId);

    Optional<Review> findByUserIdAndItemId(Long userId, Long itemId);

    /** Scoped by ownership — see other *ByIdAndClientId/*ByIdAndUserId lookups for the same "wrong id and someone else's row return the same 404" reasoning. */
    Optional<Review> findByIdAndUserId(Long id, Long userId);
}
