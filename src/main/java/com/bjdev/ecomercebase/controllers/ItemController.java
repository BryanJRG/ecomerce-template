package com.bjdev.ecomercebase.controllers;

import com.bjdev.ecomercebase.dto.request.ItemCreateRequest;
import com.bjdev.ecomercebase.dto.request.ItemUpdateRequest;
import com.bjdev.ecomercebase.dto.response.ItemResponse;
import com.bjdev.ecomercebase.services.interfaces.ItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/** Protected by hasRole('ADMIN') + the Cloudflare tunnel filter (see SecurityConfig). */
@RestController
@RequestMapping("/api/admin/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @PostMapping
    public ResponseEntity<ItemResponse> create(@Valid @RequestBody ItemCreateRequest request) {
        return ResponseEntity.ok(itemService.createItem(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ItemResponse> update(@PathVariable Long id, @RequestBody ItemUpdateRequest request) {
        return ResponseEntity.ok(itemService.updateItem(id, request));
    }

    @PostMapping("/{id}/deactivate")
    public ResponseEntity<ItemResponse> deactivate(@PathVariable Long id) {
        return ResponseEntity.ok(itemService.deactivateItem(id));
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<ItemResponse> activate(@PathVariable Long id) {
        return ResponseEntity.ok(itemService.activateItem(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(itemService.getItem(id));
    }
}
