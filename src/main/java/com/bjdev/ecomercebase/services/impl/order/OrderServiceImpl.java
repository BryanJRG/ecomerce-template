package com.bjdev.ecomercebase.services.impl.order;

import com.bjdev.ecomercebase.dto.response.OrderLineResponse;
import com.bjdev.ecomercebase.dto.response.OrderResponse;
import com.bjdev.ecomercebase.exception.NotFoundException;
import com.bjdev.ecomercebase.models.order.Order;
import com.bjdev.ecomercebase.models.order.OrderLine;
import com.bjdev.ecomercebase.repositories.order.OrderLineRepository;
import com.bjdev.ecomercebase.repositories.order.OrderRepository;
import com.bjdev.ecomercebase.services.interfaces.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderLineRepository orderLineRepository;

    @Override
    public List<OrderResponse> listOrders(Long clientId) {
        return orderRepository.findByClientIdOrderByCreatedAtDesc(clientId).stream()
                .map(order -> toResponse(order, orderLineRepository.findByOrderId(order.getId())))
                .toList();
    }

    @Override
    public OrderResponse getOrder(Long clientId, Long orderId) {
        Order order = orderRepository.findByIdAndClientId(orderId, clientId).orElseThrow(NotFoundException::order);
        return toResponse(order, orderLineRepository.findByOrderId(order.getId()));
    }

    private OrderResponse toResponse(Order order, List<OrderLine> lines) {
        List<OrderLineResponse> lineResponses = lines.stream()
                .map(l -> new OrderLineResponse(l.getVariant().getId(), l.getItemNameSnapshot(), l.getQuantity(), l.getPriceAtPurchase()))
                .toList();
        return new OrderResponse(order.getId(), order.getStatus(), order.getTotal(), order.getTrackingNumber(),
                order.getCreatedAt(), lineResponses);
    }
}
