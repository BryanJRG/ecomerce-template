package com.bjdev.ecomercebase.dto.response;

import java.time.LocalDateTime;

public record ReviewResponse(
        Long id,
        Long itemId,
        Long userId,
        Integer rating,
        String comment,
        Boolean verifiedPurchase,
        long helpfulCount,
        long notHelpfulCount,
        LocalDateTime createdAt
) {
}
