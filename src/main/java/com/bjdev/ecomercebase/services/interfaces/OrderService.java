package com.bjdev.ecomercebase.services.interfaces;

import com.bjdev.ecomercebase.dto.response.OrderResponse;

import java.util.List;

public interface OrderService {

    List<OrderResponse> listOrders(Long clientId);

    /** Scoped by ownership — see OrderRepository.findByIdAndClientId. */
    OrderResponse getOrder(Long clientId, Long orderId);
}
