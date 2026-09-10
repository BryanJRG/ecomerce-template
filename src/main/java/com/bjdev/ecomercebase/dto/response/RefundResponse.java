package com.bjdev.ecomercebase.dto.response;

import com.bjdev.ecomercebase.models.enums.RefundStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record RefundResponse(
        Long id,
        Long orderId,
        RefundStatus status,
        String reason,
        BigDecimal totalAmount,
        LocalDateTime createdAt,
        List<RefundLineResponse> lines
) {
}
