package com.bjdev.ecomercebase.dto.response;

import java.math.BigDecimal;

/** Surfaced by CartServiceImpl.cloneCartOnFailedCheckout so the frontend can tell the customer prices moved. */
public record PriceChangeNotice(Long variantId, String variantName, BigDecimal oldPrice, BigDecimal newPrice) {
}
