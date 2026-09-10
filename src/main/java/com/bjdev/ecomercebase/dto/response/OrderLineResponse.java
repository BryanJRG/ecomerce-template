package com.bjdev.ecomercebase.dto.response;

import java.math.BigDecimal;

public record OrderLineResponse(Long variantId, String itemName, Integer quantity, BigDecimal priceAtPurchase) {
}
