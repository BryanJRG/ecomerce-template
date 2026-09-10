package com.bjdev.ecomercebase.controllers;

import com.bjdev.ecomercebase.dto.request.RefundCreateRequest;
import com.bjdev.ecomercebase.dto.response.RefundResponse;
import com.bjdev.ecomercebase.exception.NotFoundException;
import com.bjdev.ecomercebase.services.impl.client.ClientResolver;
import com.bjdev.ecomercebase.services.interfaces.RefundService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Customer-facing refund requests — requires a logged-in session, same as OrderController. */
@RestController
@RequestMapping("/api/refunds")
@RequiredArgsConstructor
public class RefundController {

    private final RefundService refundService;
    private final ClientResolver clientResolver;

    @PostMapping
    public ResponseEntity<RefundResponse> request(@Valid @RequestBody RefundCreateRequest request) {
        Long clientId = clientResolver.getOrCreateCurrentClient().getId();
        return ResponseEntity.ok(refundService.requestRefund(clientId, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RefundResponse> get(@PathVariable Long id) {
        Long clientId = clientResolver.findCurrentClient().orElseThrow(NotFoundException::order).getId();
        return ResponseEntity.ok(refundService.getRefund(clientId, id));
    }

    @GetMapping
    public ResponseEntity<List<RefundResponse>> listForOrder(@RequestParam Long orderId) {
        Long clientId = clientResolver.findCurrentClient().orElseThrow(NotFoundException::order).getId();
        return ResponseEntity.ok(refundService.listRefundsForOrder(clientId, orderId));
    }
}
