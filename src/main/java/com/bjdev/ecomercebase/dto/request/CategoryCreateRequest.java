package com.bjdev.ecomercebase.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public record CategoryCreateRequest(
        @NotBlank String name,
        @NotBlank String slug,
        BigDecimal minPrice,
        BigDecimal maxPrice
) {
}
