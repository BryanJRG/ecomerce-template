package com.bjdev.base.dto.response;

import java.time.LocalDateTime;
import java.util.Map;

public record NotificationResponse(
        Long id,
        String type,
        String title,
        String message,
        String relatedTable,
        Long relatedId,
        Map<String, Object> data,
        Boolean isRead,
        LocalDateTime readAt,
        LocalDateTime createdAt,
        Long actorId,
        String actorEmail
) {
}
