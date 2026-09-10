package com.bjdev.ecomercebase.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/** Both fields optional — only non-null ones are applied, same pattern as CategoryUpdateRequest. */
public record ReviewUpdateRequest(
        @Min(1) @Max(5) Integer rating,
        String comment
) {
}
