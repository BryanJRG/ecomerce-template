package com.bjdev.ecomercebase.controllers;

import com.bjdev.ecomercebase.dto.request.AddToCartRequest;
import com.bjdev.ecomercebase.dto.response.CartResponse;
import com.bjdev.ecomercebase.services.impl.client.ClientResolver;
import com.bjdev.ecomercebase.services.interfaces.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Requires a logged-in session (falls under SecurityConfig's default anyRequest().authenticated())
 * — guest checkout is not wired yet, see ClientResolver's class doc.
 */
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final ClientResolver clientResolver;

    @PostMapping("/items")
    public ResponseEntity<CartResponse> addToCart(@Valid @RequestBody AddToCartRequest request) {
        Long clientId = clientResolver.getOrCreateCurrentClient().getId();
        return ResponseEntity.ok(cartService.addToCart(clientId, request.variantId(), request.quantity()));
    }

    @GetMapping
    public ResponseEntity<CartResponse> getActiveCart() {
        Long clientId = clientResolver.getOrCreateCurrentClient().getId();
        return ResponseEntity.ok(cartService.getActiveCart(clientId));
    }
}
