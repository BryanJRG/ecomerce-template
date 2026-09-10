package com.bjdev.ecomercebase.repositories.analytics;

import com.bjdev.ecomercebase.models.analytics.DailySalesSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface DailySalesSummaryRepository extends JpaRepository<DailySalesSummary, Long> {

    List<DailySalesSummary> findBySalesDateBetweenOrderBySalesDateAsc(LocalDate from, LocalDate to);

    /**
     * Atomic increment of an existing day's row, same "conditional UPDATE checked by affected-row
     * count" idiom as ItemVariantRepository.reserveStock — the affected-row count tells the caller
     * whether the day's row already existed (0 means it must be inserted, see
     * SalesSummaryCoordinator).
     */
    @Modifying
    @Query("UPDATE DailySalesSummary s SET s.orderCount = s.orderCount + 1, s.grossRevenue = s.grossRevenue + :amount "
            + "WHERE s.salesDate = :date")
    int incrementSummary(@Param("date") LocalDate date, @Param("amount") BigDecimal amount);
}
