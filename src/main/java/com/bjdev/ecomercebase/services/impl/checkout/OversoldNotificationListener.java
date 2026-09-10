package com.bjdev.ecomercebase.services.impl.checkout;

import com.bjdev.ecomercebase.events.OrderOversoldAdminAlertEvent;
import com.bjdev.ecomercebase.events.OrderOversoldCustomerNotificationEvent;
import com.bjdev.ecomercebase.services.interfaces.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * The two oversold notifications are handled by separate methods (each its own AFTER_COMMIT +
 * @Async listener) precisely so they fail independently — the admin alert must go out even if
 * the customer's mail provider is down, and vice versa. Neither can roll back the Order, which
 * is already committed by the time either of these runs.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OversoldNotificationListener {

    private final EmailService emailService;

    @Value("${app.mail.ops-admin:ops-admin@ecomerce-base.local}")
    private String opsAdminEmail;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCustomerNotification(OrderOversoldCustomerNotificationEvent event) {
        try {
            emailService.sendOrderOversoldCustomerEmail(event.clientEmail(), event.orderId());
        } catch (Exception e) {
            log.error("Failed to send oversold customer notification for order {}: {}",
                    event.orderId(), e.getMessage(), e);
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAdminAlert(OrderOversoldAdminAlertEvent event) {
        try {
            emailService.sendOrderOversoldAdminAlert(opsAdminEmail, event.orderId(), event.affectedVariantIds().toString());
        } catch (Exception e) {
            log.error("Failed to send oversold admin alert for order {}: {}", event.orderId(), e.getMessage(), e);
        }
    }
}
