package com.bjdev.ecomercebase.controllers;

import com.bjdev.ecomercebase.dto.response.DailySalesResponse;
import com.bjdev.ecomercebase.dto.response.MonthlySalesResponse;
import com.bjdev.ecomercebase.services.interfaces.SalesAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/** Protected by hasRole('ADMIN') + the Cloudflare tunnel filter (see SecurityConfig). */
@RestController
@RequestMapping("/api/admin/sales")
@RequiredArgsConstructor
public class AdminSalesController {

    private final SalesAnalyticsService salesAnalyticsService;

    @GetMapping("/daily")
    public ResponseEntity<List<DailySalesResponse>> daily(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(salesAnalyticsService.getDailySales(from, to));
    }

    @GetMapping("/monthly")
    public ResponseEntity<List<MonthlySalesResponse>> monthly(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(salesAnalyticsService.getMonthlySales(from, to));
    }
}
