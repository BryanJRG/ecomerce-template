package com.bjdev.base.services.interfaces;

import com.bjdev.base.dto.response.NotificationResponse;
import com.bjdev.base.models.user.User;

import java.util.List;
import java.util.Map;

/**
 * Generic, feature-agnostic notification module: any part of the system can call these methods
 * to notify one user or every (active) user holding a given role, without knowing anything about
 * notifications' storage or delivery. Callers should trigger these as secondary/best-effort side
 * effects of their own domain events (see AdminInvitationNotificationListener for the pattern),
 * never as part of the core transaction the notification is about.
 */
public interface NotificationService {

    NotificationResponse notifyUser(User recipient, User actor, String type, String title, String message,
                                     String relatedTable, Long relatedId, Map<String, Object> data);

    /** Notifies every active user holding {@code roleName}, skipping {@code actor} if they hold it too. */
    List<NotificationResponse> notifyRole(String roleName, User actor, String type, String title, String message,
                                           String relatedTable, Long relatedId, Map<String, Object> data);

    List<NotificationResponse> listForCurrentUser(boolean unreadOnly);


    long countUnreadForCurrentUser();

    NotificationResponse markAsRead(Long notificationId);

    void markAllAsRead();
}
