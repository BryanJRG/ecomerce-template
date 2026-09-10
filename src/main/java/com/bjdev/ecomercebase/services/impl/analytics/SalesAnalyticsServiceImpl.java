package com.bjdev.ecomercebase.services.impl.analytics;

import com.bjdev.ecomercebase.dto.response.DailySalesResponse;
import com.bjdev.ecomercebase.dto.response.MonthlySalesResponse;
import com.bjdev.ecomercebase.models.analytics.DailySalesSummary;
import com.bjdev.ecomercebase.repositories.analytics.DailySalesSummaryRepository;
import com.bjdev.ecomercebase.services.interfaces.SalesAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SalesAnalyticsServiceImpl implements SalesAnalyticsService {

    private final DailySalesSummaryRepository dailySalesSummaryRepository;

    @Override
    public List<DailySalesResponse> getDailySales(LocalDate from, LocalDate to) {
        return dailySalesSummaryRepository.findBySalesDateBetweenOrderBySalesDateAsc(from, to).stream()
                .map(s -> new DailySalesResponse(s.getSalesDate(), s.getOrderCount(), s.getGrossRevenue()))
                .toList();
    }

    @Override
    public List<MonthlySalesResponse> getMonthlySales(LocalDate from, LocalDate to) {
        var byMonth = dailySalesSummaryRepository.findBySalesDateBetweenOrderBySalesDateAsc(from, to).stream()
                .collect(Collectors.groupingBy(s -> YearMonth.from(s.getSalesDate()), LinkedHashMap::new, Collectors.toList()));

        return byMonth.entrySet().stream()
                .map(e -> new MonthlySalesResponse(
                        e.getKey(),
                        e.getValue().stream().mapToInt(DailySalesSummary::getOrderCount).sum(),
                        e.getValue().stream().map(DailySalesSummary::getGrossRevenue).reduce(BigDecimal.ZERO, BigDecimal::add)))
                .toList();
    }
}
