package com.bjdev.ecomercebase.repositories.checkout;

import com.bjdev.ecomercebase.models.checkout.StockReservation;
import com.bjdev.ecomercebase.models.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface StockReservationRepository extends JpaRepository<StockReservation, Long> {

    /** Orphaned reservations: still PENDING (no payment outcome ever recorded) past the timeout. */
    List<StockReservation> findByStatusAndReservedAtBefore(ReservationStatus status, LocalDateTime threshold);

    /** Table hygiene: resolved reservations are historical noise past the same retention window used for carts. */
    List<StockReservation> findByStatusInAndResolvedAtBefore(List<ReservationStatus> statuses, LocalDateTime threshold);
}
