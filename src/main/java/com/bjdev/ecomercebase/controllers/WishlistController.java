package com.bjdev.ecomercebase.controllers;

import com.bjdev.ecomercebase.dto.response.WishlistLineResponse;
import com.bjdev.ecomercebase.security.CurrentUserProvider;
import com.bjdev.ecomercebase.services.interfaces.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Account-only (no guest equivalent) — requires a logged-in session, tied to User rather than Client. */
@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping
    public ResponseEntity<List<WishlistLineResponse>> list() {
        return ResponseEntity.ok(wishlistService.listWishlist(currentUserProvider.getCurrentUser().getId()));
    }

    @PostMapping("/items/{variantId}")
    public ResponseEntity<List<WishlistLineResponse>> add(@PathVariable Long variantId) {
        return ResponseEntity.ok(wishlistService.addToWishlist(currentUserProvider.getCurrentUser().getId(), variantId));
    }

    @DeleteMapping("/items/{variantId}")
    public ResponseEntity<List<WishlistLineResponse>> remove(@PathVariable Long variantId) {
        return ResponseEntity.ok(wishlistService.removeFromWishlist(currentUserProvider.getCurrentUser().getId(), variantId));
    }
}
