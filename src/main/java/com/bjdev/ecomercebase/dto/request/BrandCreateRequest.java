package com.bjdev.ecomercebase.dto.request;

import jakarta.validation.constraints.NotBlank;

public record BrandCreateRequest(@NotBlank String name, String logoUrl) {
}
