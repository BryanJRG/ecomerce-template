package com.bjdev.ecomercebase.models.enums;

/**
 * Canonical shape of what a {@link com.bjdev.ecomercebase.models.discount.Discount} targets. See
 * Discount's class doc for why this is a single resolved enum rather than combinable flags.
 */
public enum DiscountTargetType {
    ALL,
    CATEGORY,
    ITEM,
    VARIANT
}
