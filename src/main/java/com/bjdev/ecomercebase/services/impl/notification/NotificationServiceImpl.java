package com.bjdev.ecomercebase.services.impl.notification;

import com.bjdev.ecomercebase.dto.response.NotificationResponse;
import com.bjdev.ecomercebase.exception.NotFoundException;
import com.bjdev.ecomercebase.models.notification.Notification;
import com.bjdev.ecomercebase.models.user.User;
import com.bjdev.ecomercebase.repositories.notification.NotificationRepository;
import com.bjdev.ecomercebase.repositories.user.UserRepository;
import com.bjdev.ecomercebase.security.CurrentUserProvider;
import com.bjdev.ecomercebase.services.interfaces.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final CurrentUserProvider currentUserProvider;

    @Override
    // REQUIRES_NEW: notification delivery is a secondary effect and must persist (or fail) on its
    // own — it never rides on, and never aborts, the transaction of the action that triggered it.
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public NotificationResponse notifyUser(User recipient, User actor, String type, String title, String message,
                                            String relatedTable, Long relatedId, Map<String, Object> data) {
        Notification notification = Notification.builder()
                .recipient(recipient)
                .actor(actor)
                .type(type)
                .title(title)
                .message(message)
                .relatedTable(relatedTable)
                .relatedId(relatedId)
                .data(data != null ? data : new HashMap<>())
                .isRead(false)
                .build();

        return toResponse(notificationRepository.save(notification));
    }


    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<NotificationResponse> notifyRole(String roleName, User actor, String type, String title, String message,
                                                  String relatedTable, Long relatedId, Map<String, Object> data) {
        return userRepository.findByRoles_NameAndIsActiveTrue(roleName).stream()
                .filter(user -> actor == null || !user.getId().equals(actor.getId()))
                .map(user -> notifyUser(user, actor, type, title, message, relatedTable, relatedId, data))
                .toList();
    }

    @Override
    public List<NotificationResponse> listForCurrentUser(boolean unreadOnly) {
        User user = currentUserProvider.getCurrentUser();
        List<Notification> notifications = unreadOnly
                ? notificationRepository.findTop50ByRecipientAndIsReadFalseOrderByCreatedAtDesc(user)
                : notificationRepository.findTop50ByRecipientOrderByCreatedAtDesc(user);

        return notifications.stream().map(this::toResponse).toList();
    }

    @Override
    public long countUnreadForCurrentUser() {
        return notificationRepository.countByRecipientAndIsReadFalse(currentUserProvider.getCurrentUser());
    }

    @Override
    @Transactional
    public NotificationResponse markAsRead(Long notificationId) {
        User user = currentUserProvider.getCurrentUser();
        Notification notification = notificationRepository.findByIdAndRecipient(notificationId, user)
                .orElseThrow(NotFoundException::notification);

        if (!Boolean.TRUE.equals(notification.getIsRead())) {
            notification.setIsRead(true);
            notification.setReadAt(LocalDateTime.now());
            notification = notificationRepository.save(notification);
        }

        return toResponse(notification);
    }

    @Override
    @Transactional
    public void markAllAsRead() {
        User user = currentUserProvider.getCurrentUser();
        LocalDateTime now = LocalDateTime.now();

        List<Notification> unread = notificationRepository.findAllByRecipientAndIsReadFalse(user);
        unread.forEach(n -> {
            n.setIsRead(true);
            n.setReadAt(now);
        });
        notificationRepository.saveAll(unread);
    }

    private NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getType(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getRelatedTable(),
                notification.getRelatedId(),
                notification.getData(),
                notification.getIsRead(),
                notification.getReadAt(),
                notification.getCreatedAt(),
                notification.getActor() != null ? notification.getActor().getId() : null,
                notification.getActor() != null ? notification.getActor().getEmail() : null
        );
    }

}
