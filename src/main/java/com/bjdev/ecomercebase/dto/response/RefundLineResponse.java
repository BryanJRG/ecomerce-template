package com.bjdev.ecomercebase.dto.response;

import java.math.BigDecimal;

public record RefundLineResponse(Long orderLineId, String itemName, Integer quantity, BigDecimal amount) {
}
