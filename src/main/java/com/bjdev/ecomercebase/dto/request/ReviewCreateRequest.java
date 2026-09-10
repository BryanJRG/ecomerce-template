package com.bjdev.ecomercebase.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ReviewCreateRequest(
        @NotNull Long itemId,
        @NotNull @Min(1) @Max(5) Integer rating,
        String comment
) {
}
