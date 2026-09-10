package com.bjdev.ecomercebase.services.impl.checkout;

import com.bjdev.ecomercebase.events.StockReleasedEvent;
import com.bjdev.ecomercebase.exception.CheckoutException;
import com.bjdev.ecomercebase.models.cart.Cart;
import com.bjdev.ecomercebase.models.cart.CartLine;
import com.bjdev.ecomercebase.models.checkout.StockReservation;
import com.bjdev.ecomercebase.models.enums.ReservationStatus;
import com.bjdev.ecomercebase.repositories.catalog.ItemVariantRepository;
import com.bjdev.ecomercebase.repositories.checkout.StockReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Owns the full lifecycle of a {@link StockReservation}: reserve (atomic, before any payment
 * provider is called), confirm (payment approved, Order created) and release (payment declined,
 * or the reservation orphaned past its timeout). Shared by CheckoutServiceImpl and
 * StockReservationCleanupJob so both paths release stock through the exact same code.
 */
@Component
@RequiredArgsConstructor
public class StockReservationCoordinator {

    private final ItemVariantRepository itemVariantRepository;
    private final StockReservationRepository stockReservationRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Runs in its own transaction, committed independently of the payment call that follows —
     * the reservation must be durable BEFORE we ever hand control to a provider that might hang
     * (e.g. a Wompi 3DS challenge the customer never completes).
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<StockReservation> reserveForCart(Cart cart, List<CartLine> lines, String idempotencyKey) {
        return lines.stream()
                .map(line -> reserveLine(cart, line, idempotencyKey))
                .toList();
    }

    private StockReservation reserveLine(Cart cart, CartLine line, String idempotencyKey) {
        int updated = itemVariantRepository.reserveStock(line.getVariant().getId(), line.getQuantity());
        if (updated == 0) {
            throw CheckoutException.outOfStock(line.getVariant().getName());
        }

        return stockReservationRepository.save(StockReservation.builder()
                .variant(line.getVariant())
                .quantity(line.getQuantity())
                .cart(cart)
                .idempotencyKey(idempotencyKey)
                .status(ReservationStatus.PENDING)
                .build());
    }

    @Transactional
    public void confirmReservations(List<StockReservation> reservations) {
        LocalDateTime now = LocalDateTime.now();
        reservations.forEach(r -> {
            r.setStatus(ReservationStatus.CONFIRMED);
            r.setResolvedAt(now);
        });
        stockReservationRepository.saveAll(reservations);
    }

    /** Marks each reservation RELEASED and publishes the compensation event that performs the actual stock revert. */
    @Transactional
    public void releaseAndPublish(List<StockReservation> reservations) {
        LocalDateTime now = LocalDateTime.now();
        for (StockReservation reservation : reservations) {
            reservation.setStatus(ReservationStatus.RELEASED);
            reservation.setResolvedAt(now);
            stockReservationRepository.save(reservation);
            eventPublisher.publishEvent(new StockReleasedEvent(
                    reservation.getId(), reservation.getVariant().getId(), reservation.getQuantity()));
        }
    }
}
