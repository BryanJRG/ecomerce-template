package com.bjdev.ecomercebase.services.interfaces;

import com.bjdev.ecomercebase.dto.response.WishlistLineResponse;

import java.util.List;

public interface WishlistService {

    /** Idempotent: adding a variant already on the wishlist is a no-op, not an error. */
    List<WishlistLineResponse> addToWishlist(Long userId, Long variantId);

    List<WishlistLineResponse> removeFromWishlist(Long userId, Long variantId);

    List<WishlistLineResponse> listWishlist(Long userId);
}
