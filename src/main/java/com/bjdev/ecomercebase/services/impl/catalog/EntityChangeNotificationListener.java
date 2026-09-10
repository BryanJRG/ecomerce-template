package com.bjdev.ecomercebase.services.impl.catalog;

import com.bjdev.ecomercebase.events.EntityChangedEvent;
import com.bjdev.ecomercebase.models.user.User;
import com.bjdev.ecomercebase.repositories.user.UserRepository;
import com.bjdev.ecomercebase.services.interfaces.EmailService;
import com.bjdev.ecomercebase.services.interfaces.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Map;

/**
 * Generic "glue" layer for every admin-managed entity that publishes EntityChangedEvent
 * (Item/Category/Brand/Discount today, any future one with zero new code here) — notifies the
 * catalog admins by email and in-app, same pattern as AdminInvitationNotificationListener.
 * @Async because, like the email-sending part of the old ItemNotificationListener, a slow/failing
 * SMTP call must never add latency to the request that triggered it.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EntityChangeNotificationListener {

    private static final String ADMIN_ROLE_NAME = "ADMIN";

    private static final Map<String, String> ENTITY_LABELS = Map.of(
            "ITEM", "el ítem",
            "CATEGORY", "la categoría",
            "BRAND", "la marca",
            "DISCOUNT", "el descuento",
            "REFUND", "el reembolso"
    );

    private static final Map<String, String> ACTION_VERBS = Map.of(
            "CREATED", "creó",
            "UPDATED", "actualizó",
            "DEACTIVATED", "desactivó",
            "ACTIVATED", "reactivó"
    );

    private final EmailService emailService;
    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @Value("${app.mail.catalog-admin:catalog-admin@ecomerce-base.local}")
    private String catalogAdminEmail;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onEntityChanged(EntityChangedEvent event) {
        try {
            User actor = event.actorId() != null ? userRepository.findById(event.actorId()).orElse(null) : null;
            String message = buildMessage(event);
            String subject = "Catálogo: cambio en " + tableNameFor(event.entityType());

            emailService.sendPlainEmail(catalogAdminEmail, subject, message);
            notificationService.notifyRole(ADMIN_ROLE_NAME, actor,
                    "catalog." + event.entityType().toLowerCase() + "." + event.action().name().toLowerCase(),
                    subject, message, tableNameFor(event.entityType()), event.entityId(), Map.of());
        } catch (Exception e) {
            log.error("Failed to notify about {} {} {}: {}", event.action(), event.entityType(), event.entityId(), e.getMessage(), e);
        }
    }

    private String buildMessage(EntityChangedEvent event) {
        String entityLabel = ENTITY_LABELS.getOrDefault(event.entityType(), "el registro");
        String verb = ACTION_VERBS.getOrDefault(event.action().name(), "modificó");
        return "Se " + verb + " " + entityLabel + " \"" + event.entityLabel() + "\" (id " + event.entityId() + ").";
    }

    private String tableNameFor(String entityType) {
        return switch (entityType) {
            case "ITEM" -> "items";
            case "CATEGORY" -> "categories";
            case "BRAND" -> "brands";
            case "DISCOUNT" -> "discounts";
            case "REFUND" -> "refunds";
            default -> entityType.toLowerCase() + "s";
        };
    }
}
