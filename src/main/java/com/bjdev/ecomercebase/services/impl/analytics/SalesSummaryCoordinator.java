package com.bjdev.ecomercebase.services.impl.analytics;

import com.bjdev.ecomercebase.models.analytics.DailySalesSummary;
import com.bjdev.ecomercebase.repositories.analytics.DailySalesSummaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Kept as its own bean (rather than a private method on the listener) so its own
 * {@code @Transactional} boundary applies — called from an AFTER_COMMIT listener, so it must run in
 * REQUIRES_NEW (there's no transaction left to join, per the domain-events convention).
 */
@Component
@RequiredArgsConstructor
public class SalesSummaryCoordinator {

    private final DailySalesSummaryRepository dailySalesSummaryRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordOrderPaid(LocalDate salesDate, BigDecimal amount) {
        if (dailySalesSummaryRepository.incrementSummary(salesDate, amount) > 0) {
            return;
        }
        try {
            dailySalesSummaryRepository.save(DailySalesSummary.builder()
                    .salesDate(salesDate)
                    .orderCount(1)
                    .grossRevenue(amount)
                    .build());
        } catch (DataIntegrityViolationException e) {
            // Another concurrent order for the same day inserted the row first — retry as an update.
            dailySalesSummaryRepository.incrementSummary(salesDate, amount);
        }
    }
}
