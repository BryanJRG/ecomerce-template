package com.bjdev.ecomercebase.services.impl.checkout;

import com.bjdev.ecomercebase.models.checkout.StockReservation;
import com.bjdev.ecomercebase.models.enums.ReservationStatus;
import com.bjdev.ecomercebase.repositories.checkout.StockReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Safety net for the gap between "stock reserved" and "payment outcome known" (e.g. a customer
 * who abandons a Wompi 3DS challenge halfway through, so no webhook confirmation/failure ever
 * arrives). Any reservation still PENDING past the configured timeout is released — see
 * StockReservation's class doc for why this can't just live on Payment.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StockReservationCleanupJob {

    private final StockReservationRepository stockReservationRepository;
    private final StockReservationCoordinator stockReservationCoordinator;

    @Value("${app.checkout.reservation-timeout-minutes:15}")
    private int reservationTimeoutMinutes;

    @Value("${app.checkout.reservation-retention-days:30}")
    private int reservationRetentionDays;

    @Scheduled(fixedDelayString = "${app.checkout.reservation-cleanup-interval-ms:300000}")
    public void releaseOrphanedReservations() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(reservationTimeoutMinutes);
        List<StockReservation> orphaned = stockReservationRepository
                .findByStatusAndReservedAtBefore(ReservationStatus.PENDING, threshold);

        if (orphaned.isEmpty()) {
            return;
        }

        stockReservationCoordinator.releaseAndPublish(orphaned);
        log.warn("Released {} orphaned stock reservation(s) older than {} minute(s)", orphaned.size(), reservationTimeoutMinutes);
    }

    /**
     * Resolved reservations (CONFIRMED/RELEASED) are a checkout-internal reconciliation trail, not
     * an accounting record — the Order/Payment rows they led to are the durable source of truth.
     * Hard-deleting old ones here keeps the table bounded, the same way CartServiceImpl purges
     * abandoned carts.
     */
    @Scheduled(cron = "${app.checkout.reservation-purge-cron:0 30 3 1 * *}")
    @Transactional
    public void purgeResolvedReservations() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(reservationRetentionDays);
        List<StockReservation> resolved = stockReservationRepository.findByStatusInAndResolvedAtBefore(
                List.of(ReservationStatus.CONFIRMED, ReservationStatus.RELEASED), threshold);

        if (resolved.isEmpty()) {
            return;
        }

        stockReservationRepository.deleteAll(resolved);
        log.info("Purged {} resolved stock reservation(s) older than {} day(s)", resolved.size(), reservationRetentionDays);
    }
}
