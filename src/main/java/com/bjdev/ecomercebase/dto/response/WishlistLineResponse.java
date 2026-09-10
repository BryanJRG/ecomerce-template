package com.bjdev.ecomercebase.dto.response;

import java.math.BigDecimal;

public record WishlistLineResponse(Long variantId, String itemName, String variantName, BigDecimal price, Boolean active) {
}
