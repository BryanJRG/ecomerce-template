package com.bjdev.ecomercebase.controllers;

import com.bjdev.ecomercebase.dto.request.DiscountCreateRequest;
import com.bjdev.ecomercebase.dto.request.DiscountUpdateRequest;
import com.bjdev.ecomercebase.dto.response.DiscountResponse;
import com.bjdev.ecomercebase.services.interfaces.DiscountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Protected by hasRole('ADMIN') + the Cloudflare tunnel filter (see SecurityConfig). Discounts have
 * no dedicated deactivate endpoint — set {@code active: false} through the regular update, see
 * DiscountUpdateRequest's class doc.
 */
@RestController
@RequestMapping("/api/admin/discounts")
@RequiredArgsConstructor
public class DiscountController {

    private final DiscountService discountService;

    @PostMapping
    public ResponseEntity<DiscountResponse> create(@Valid @RequestBody DiscountCreateRequest request) {
        return ResponseEntity.ok(discountService.createDiscount(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DiscountResponse> update(@PathVariable Long id, @RequestBody DiscountUpdateRequest request) {
        return ResponseEntity.ok(discountService.updateDiscount(id, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DiscountResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(discountService.getDiscount(id));
    }

    @GetMapping
    public ResponseEntity<List<DiscountResponse>> list() {
        return ResponseEntity.ok(discountService.listDiscounts());
    }
}
