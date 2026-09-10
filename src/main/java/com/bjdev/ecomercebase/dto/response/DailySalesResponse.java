package com.bjdev.ecomercebase.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DailySalesResponse(LocalDate date, Integer orderCount, BigDecimal grossRevenue) {
}
