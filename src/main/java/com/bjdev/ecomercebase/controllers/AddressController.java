package com.bjdev.ecomercebase.controllers;

import com.bjdev.ecomercebase.dto.request.ShippingAddressCreateRequest;
import com.bjdev.ecomercebase.dto.request.ShippingAddressUpdateRequest;
import com.bjdev.ecomercebase.dto.response.ShippingAddressResponse;
import com.bjdev.ecomercebase.exception.NotFoundException;
import com.bjdev.ecomercebase.services.impl.client.ClientResolver;
import com.bjdev.ecomercebase.services.interfaces.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** "My addresses" — requires a logged-in session, same as CartController/OrderController. */
@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;
    private final ClientResolver clientResolver;

    @PostMapping
    public ResponseEntity<ShippingAddressResponse> create(@Valid @RequestBody ShippingAddressCreateRequest request) {
        Long clientId = clientResolver.getOrCreateCurrentClient().getId();
        return ResponseEntity.ok(addressService.createAddress(clientId, request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ShippingAddressResponse> update(@PathVariable Long id, @RequestBody ShippingAddressUpdateRequest request) {
        Long clientId = clientResolver.findCurrentClient().orElseThrow(NotFoundException::shippingAddress).getId();
        return ResponseEntity.ok(addressService.updateAddress(clientId, id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Long clientId = clientResolver.findCurrentClient().orElseThrow(NotFoundException::shippingAddress).getId();
        addressService.deleteAddress(clientId, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<ShippingAddressResponse>> list() {
        return ResponseEntity.ok(clientResolver.findCurrentClient()
                .map(client -> addressService.listAddresses(client.getId()))
                .orElse(List.of()));
    }
}
