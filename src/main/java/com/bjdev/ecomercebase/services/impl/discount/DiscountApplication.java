package com.bjdev.ecomercebase.services.impl.discount;

import com.bjdev.ecomercebase.models.discount.Discount;

import java.math.BigDecimal;

/** The discount CheckoutServiceImpl resolved for a cart, and exactly how much it takes off. */
public record DiscountApplication(Discount discount, BigDecimal amount) {
}
