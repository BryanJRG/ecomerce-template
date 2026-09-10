package com.bjdev.ecomercebase.controllers;

import com.bjdev.ecomercebase.dto.response.RefundResponse;
import com.bjdev.ecomercebase.services.interfaces.RefundService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Protected by hasRole('ADMIN') + the Cloudflare tunnel filter (see SecurityConfig). Approving/
 * completing a refund never talks to the payment provider — see RefundService's class doc.
 */
@RestController
@RequestMapping("/api/admin/refunds")
@RequiredArgsConstructor
public class AdminRefundController {

    private final RefundService refundService;

    @GetMapping
    public ResponseEntity<List<RefundResponse>> list() {
        return ResponseEntity.ok(refundService.listAllRefunds());
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<RefundResponse> approve(@PathVariable Long id) {
        return ResponseEntity.ok(refundService.approveRefund(id));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<RefundResponse> reject(@PathVariable Long id) {
        return ResponseEntity.ok(refundService.rejectRefund(id));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<RefundResponse> complete(@PathVariable Long id) {
        return ResponseEntity.ok(refundService.completeRefund(id));
    }
}
