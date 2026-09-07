package com.bjdev.base.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record UserResponse(
        Long id,
        String email,
        String userName,
        String firstName,
        String lastName,
        Boolean isActive,
        Boolean isVerified,
        List<String> roles,
        LocalDateTime createdAt
) {
}
