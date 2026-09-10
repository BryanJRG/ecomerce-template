package com.bjdev.ecomercebase.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AddToCartRequest(
        @NotNull Long variantId,
        @NotNull @Min(1) Integer quantity
) {
}
