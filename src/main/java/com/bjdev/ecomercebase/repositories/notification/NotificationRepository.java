package com.bjdev.ecomercebase.repositories.notification;

import com.bjdev.ecomercebase.models.notification.Notification;
import com.bjdev.ecomercebase.models.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findTop50ByRecipientOrderByCreatedAtDesc(User recipient);

    List<Notification> findTop50ByRecipientAndIsReadFalseOrderByCreatedAtDesc(User recipient);

    List<Notification> findAllByRecipientAndIsReadFalse(User recipient);

    long countByRecipientAndIsReadFalse(User recipient);

    Optional<Notification> findByIdAndRecipient(Long id, User recipient);
}
