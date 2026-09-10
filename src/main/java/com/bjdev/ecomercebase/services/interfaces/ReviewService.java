package com.bjdev.ecomercebase.services.interfaces;

import com.bjdev.ecomercebase.dto.request.ReviewCreateRequest;
import com.bjdev.ecomercebase.dto.request.ReviewUpdateRequest;
import com.bjdev.ecomercebase.dto.response.ReviewResponse;

import java.util.List;

public interface ReviewService {

    /** One review per user per item — throws ReviewException.alreadyReviewed if one already exists. */
    ReviewResponse createReview(Long userId, ReviewCreateRequest request);

    ReviewResponse updateReview(Long userId, Long reviewId, ReviewUpdateRequest request);

    void deleteReview(Long userId, Long reviewId);

    List<ReviewResponse> listReviewsForItem(Long itemId);

    ReviewResponse voteReview(Long userId, Long reviewId, boolean helpful);
}
