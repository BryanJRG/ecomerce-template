package com.bjdev.ecomercebase.repositories.cart;

import com.bjdev.ecomercebase.models.cart.Cart;
import com.bjdev.ecomercebase.models.enums.CartStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByClientIdAndStatus(Long clientId, CartStatus status);

    /** Feeds the monthly hard-delete job — see CartServiceImpl.purgeAbandonedCarts. */
    List<Cart> findByStatusAndUpdatedAtBefore(CartStatus status, LocalDateTime threshold);
}
