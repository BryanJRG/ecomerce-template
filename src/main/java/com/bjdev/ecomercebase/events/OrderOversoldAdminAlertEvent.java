package com.bjdev.ecomercebase.events;

import java.util.List;

public record OrderOversoldAdminAlertEvent(Long orderId, List<Long> affectedVariantIds) {
}
