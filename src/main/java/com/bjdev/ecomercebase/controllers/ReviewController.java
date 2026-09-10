package com.bjdev.ecomercebase.controllers;

import com.bjdev.ecomercebase.dto.request.ReviewCreateRequest;
import com.bjdev.ecomercebase.dto.request.ReviewUpdateRequest;
import com.bjdev.ecomercebase.dto.request.ReviewVoteRequest;
import com.bjdev.ecomercebase.dto.response.ReviewResponse;
import com.bjdev.ecomercebase.security.CurrentUserProvider;
import com.bjdev.ecomercebase.services.interfaces.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Listing reviews for an item is public (storefront product page); creating/editing/voting
 * requires a logged-in session, same as WishlistController. Add {@code GET /api/items/{id}/reviews}
 * to SecurityConfig's public GET matchers if you mount this path differently.
 */
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping
    public ResponseEntity<List<ReviewResponse>> listForItem(@RequestParam Long itemId) {
        return ResponseEntity.ok(reviewService.listReviewsForItem(itemId));
    }

    @PostMapping
    public ResponseEntity<ReviewResponse> create(@Valid @RequestBody ReviewCreateRequest request) {
        return ResponseEntity.ok(reviewService.createReview(currentUserProvider.getCurrentUser().getId(), request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReviewResponse> update(@PathVariable Long id, @RequestBody ReviewUpdateRequest request) {
        return ResponseEntity.ok(reviewService.updateReview(currentUserProvider.getCurrentUser().getId(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        reviewService.deleteReview(currentUserProvider.getCurrentUser().getId(), id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/vote")
    public ResponseEntity<ReviewResponse> vote(@PathVariable Long id, @Valid @RequestBody ReviewVoteRequest request) {
        return ResponseEntity.ok(reviewService.voteReview(currentUserProvider.getCurrentUser().getId(), id, request.helpful()));
    }
}
