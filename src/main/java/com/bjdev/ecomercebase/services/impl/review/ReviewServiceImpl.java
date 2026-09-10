package com.bjdev.ecomercebase.services.impl.review;

import com.bjdev.ecomercebase.dto.request.ReviewCreateRequest;
import com.bjdev.ecomercebase.dto.request.ReviewUpdateRequest;
import com.bjdev.ecomercebase.dto.response.ReviewResponse;
import com.bjdev.ecomercebase.exception.AuthException;
import com.bjdev.ecomercebase.exception.CatalogException;
import com.bjdev.ecomercebase.exception.ReviewException;
import com.bjdev.ecomercebase.models.catalog.Item;
import com.bjdev.ecomercebase.models.order.OrderLine;
import com.bjdev.ecomercebase.models.review.Review;
import com.bjdev.ecomercebase.models.review.ReviewVote;
import com.bjdev.ecomercebase.models.user.User;
import com.bjdev.ecomercebase.repositories.catalog.ItemRepository;
import com.bjdev.ecomercebase.repositories.order.OrderLineRepository;
import com.bjdev.ecomercebase.repositories.review.ReviewRepository;
import com.bjdev.ecomercebase.repositories.review.ReviewVoteRepository;
import com.bjdev.ecomercebase.repositories.user.UserRepository;
import com.bjdev.ecomercebase.services.interfaces.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewVoteRepository reviewVoteRepository;
    private final ItemRepository itemRepository;
    private final OrderLineRepository orderLineRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ReviewResponse createReview(Long userId, ReviewCreateRequest request) {
        if (reviewRepository.findByUserIdAndItemId(userId, request.itemId()).isPresent()) {
            throw ReviewException.alreadyReviewed();
        }

        Item item = itemRepository.findById(request.itemId()).orElseThrow(CatalogException::itemNotFound);
        User user = userRepository.findById(userId).orElseThrow(AuthException::invalidCredentials);
        OrderLine verifiedLine = orderLineRepository
                .findFirstByOrder_Client_UserIdAndVariant_Item_Id(userId, request.itemId())
                .orElse(null);

        Review review = Review.builder()
                .user(user)
                .item(item)
                .orderLine(verifiedLine)
                .rating(request.rating())
                .comment(request.comment())
                .build();
        review = reviewRepository.save(review);

        return toResponse(review);
    }

    @Override
    @Transactional
    public ReviewResponse updateReview(Long userId, Long reviewId, ReviewUpdateRequest request) {
        Review review = getOwnedOrThrow(userId, reviewId);
        if (request.rating() != null) review.setRating(request.rating());
        if (request.comment() != null) review.setComment(request.comment());
        review = reviewRepository.save(review);
        return toResponse(review);
    }

    @Override
    @Transactional
    public void deleteReview(Long userId, Long reviewId) {
        reviewRepository.delete(getOwnedOrThrow(userId, reviewId));
    }

    @Override
    public List<ReviewResponse> listReviewsForItem(Long itemId) {
        return reviewRepository.findByItemId(itemId).stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public ReviewResponse voteReview(Long userId, Long reviewId, boolean helpful) {
        Review review = reviewRepository.findById(reviewId).orElseThrow(ReviewException::reviewNotFound);
        User user = userRepository.findById(userId).orElseThrow(AuthException::invalidCredentials);

        ReviewVote vote = reviewVoteRepository.findByReviewIdAndUserId(reviewId, userId)
                .orElseGet(() -> ReviewVote.builder().review(review).user(user).build());
        vote.setHelpful(helpful);
        reviewVoteRepository.save(vote);

        return toResponse(review);
    }

    private Review getOwnedOrThrow(Long userId, Long reviewId) {
        return reviewRepository.findByIdAndUserId(reviewId, userId).orElseThrow(ReviewException::reviewNotFound);
    }

    private ReviewResponse toResponse(Review review) {
        long helpfulCount = reviewVoteRepository.countByReviewIdAndHelpful(review.getId(), true);
        long notHelpfulCount = reviewVoteRepository.countByReviewIdAndHelpful(review.getId(), false);
        return new ReviewResponse(review.getId(), review.getItem().getId(), review.getUser().getId(),
                review.getRating(), review.getComment(), review.getOrderLine() != null,
                helpfulCount, notHelpfulCount, review.getCreatedAt());
    }
}
