package com.bjdev.ecomercebase.dto.response;

import com.bjdev.ecomercebase.models.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        OrderStatus status,
        BigDecimal total,
        String trackingNumber,
        LocalDateTime createdAt,
        List<OrderLineResponse> lines
) {
}
