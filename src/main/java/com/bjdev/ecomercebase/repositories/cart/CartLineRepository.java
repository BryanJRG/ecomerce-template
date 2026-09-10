package com.bjdev.ecomercebase.repositories.cart;

import com.bjdev.ecomercebase.models.cart.CartLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartLineRepository extends JpaRepository<CartLine, Long> {

    List<CartLine> findByCartId(Long cartId);

    Optional<CartLine> findByCartIdAndVariantId(Long cartId, Long variantId);

    /**
     * Manual cascade: Cart deliberately has no JPA @OneToMany/cascade (see Cart's class doc), so
     * the monthly abandoned-cart purge must delete lines before the parent cart itself. A derived
     * "deleteBy" query is inherently a modifying operation in Spring Data JPA — no @Modifying/@Query needed.
     */
    void deleteByCartId(Long cartId);
}
