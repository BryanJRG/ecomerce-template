package com.bjdev.ecomercebase.services.interfaces;

import com.bjdev.ecomercebase.dto.response.DailySalesResponse;
import com.bjdev.ecomercebase.dto.response.MonthlySalesResponse;

import java.time.LocalDate;
import java.util.List;

public interface SalesAnalyticsService {

    /** One row per day in [from, to], ascending. Days with no paid orders are simply absent (not zero-filled). */
    List<DailySalesResponse> getDailySales(LocalDate from, LocalDate to);

    /**
     * Monthly rollup over the same DailySalesSummary rows as getDailySales — aggregated in memory
     * rather than via a DB-specific GROUP BY, since the table stays small (one row per day).
     */
    List<MonthlySalesResponse> getMonthlySales(LocalDate from, LocalDate to);
}
