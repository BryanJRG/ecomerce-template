package com.bjdev.ecomercebase.services.impl.checkout;

import com.bjdev.ecomercebase.events.StockReleasedEvent;
import com.bjdev.ecomercebase.repositories.catalog.ItemVariantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Sole executor of the stock reversion described by {@link StockReleasedEvent} — every release
 * path (declined payment, orphaned-reservation cleanup) publishes the event instead of touching
 * stock directly, so there is exactly one code path that ever adds stock back.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StockReleaseListener {

    private final ItemVariantRepository itemVariantRepository;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onStockReleased(StockReleasedEvent event) {
        try {
            itemVariantRepository.releaseStock(event.variantId(), event.quantity());
        } catch (Exception e) {
            log.error("Failed to release stock for reservation {} (variant {}, qty {}): {}",
                    event.stockReservationId(), event.variantId(), event.quantity(), e.getMessage(), e);
        }
    }
}
