package com.bjdev.ecomercebase.services.impl.analytics;

import com.bjdev.ecomercebase.events.OrderPaidEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Keeps DailySalesSummary in sync with paid orders. AFTER_COMMIT because this is a secondary effect
 * of an order that already committed; @Async so a slow aggregate update never adds latency to
 * checkout. Swallows failures — a broken sales summary must never surface as a checkout failure.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SalesSummaryListener {

    private final SalesSummaryCoordinator salesSummaryCoordinator;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderPaid(OrderPaidEvent event) {
        try {
            salesSummaryCoordinator.recordOrderPaid(event.paidAt().toLocalDate(), event.total());
        } catch (Exception e) {
            log.error("Failed to update daily sales summary for order {}: {}", event.orderId(), e.getMessage(), e);
        }
    }
}
