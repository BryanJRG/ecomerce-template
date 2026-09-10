package com.bjdev.ecomercebase.dto.response;

import java.math.BigDecimal;
import java.time.YearMonth;

public record MonthlySalesResponse(YearMonth month, Integer orderCount, BigDecimal grossRevenue) {
}
