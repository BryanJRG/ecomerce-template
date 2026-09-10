package com.bjdev.ecomercebase.repositories.order;

import com.bjdev.ecomercebase.models.order.OrderStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, Long> {
}
