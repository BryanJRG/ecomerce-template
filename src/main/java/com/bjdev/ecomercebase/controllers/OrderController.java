package com.bjdev.ecomercebase.controllers;

import com.bjdev.ecomercebase.dto.response.OrderResponse;
import com.bjdev.ecomercebase.exception.NotFoundException;
import com.bjdev.ecomercebase.services.impl.client.ClientResolver;
import com.bjdev.ecomercebase.services.interfaces.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** "My orders" — requires a logged-in session. Read-only: never creates a Client just to look up orders. */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final ClientResolver clientResolver;

    @GetMapping
    public ResponseEntity<List<OrderResponse>> list() {
        return ResponseEntity.ok(clientResolver.findCurrentClient()
                .map(client -> orderService.listOrders(client.getId()))
                .orElse(List.of()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> get(@PathVariable Long id) {
        Long clientId = clientResolver.findCurrentClient().orElseThrow(NotFoundException::order).getId();
        return ResponseEntity.ok(orderService.getOrder(clientId, id));
    }
}
