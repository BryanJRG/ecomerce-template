package com.bjdev.ecomercebase.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record RefundLineRequest(@NotNull Long orderLineId, @NotNull @Positive Integer quantity) {
}
