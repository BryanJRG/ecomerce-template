package com.bjdev.ecomercebase.services.interfaces;

import com.bjdev.ecomercebase.dto.response.CartResponse;
import com.bjdev.ecomercebase.dto.response.CloneCartResult;

public interface CartService {

    CartResponse addToCart(Long clientId, Long variantId, Integer quantity);

    CartResponse getActiveCart(Long clientId);

    /**
     * Marks the given cart ABANDONED and creates a fresh ACTIVE cart for the same client,
     * cloning its lines with refreshed price/stock. Called by CheckoutServiceImpl when a
     * payment is declined.
     */
    CloneCartResult cloneCartOnFailedCheckout(Long cartId);
}
