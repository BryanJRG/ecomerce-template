package com.bjdev.ecomercebase.controllers;

import com.bjdev.ecomercebase.dto.request.CheckoutRequest;
import com.bjdev.ecomercebase.dto.response.CheckoutResult;
import com.bjdev.ecomercebase.services.impl.client.ClientResolver;
import com.bjdev.ecomercebase.services.interfaces.CheckoutService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Requires a logged-in session — see CartController/ClientResolver. */
@RestController
@RequestMapping("/api/checkout")
@RequiredArgsConstructor
public class CheckoutController {

    private final CheckoutService checkoutService;
    private final ClientResolver clientResolver;

    @PostMapping
    public ResponseEntity<CheckoutResult> checkout(@Valid @RequestBody CheckoutRequest request) {
        Long clientId = clientResolver.getOrCreateCurrentClient().getId();
        return ResponseEntity.ok(checkoutService.checkout(clientId, request));
    }
}
