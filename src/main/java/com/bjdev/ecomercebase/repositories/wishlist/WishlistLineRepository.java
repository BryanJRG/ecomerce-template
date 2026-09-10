package com.bjdev.ecomercebase.repositories.wishlist;

import com.bjdev.ecomercebase.models.wishlist.WishlistLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WishlistLineRepository extends JpaRepository<WishlistLine, Long> {

    List<WishlistLine> findByWishlistId(Long wishlistId);

    Optional<WishlistLine> findByWishlistIdAndVariantId(Long wishlistId, Long variantId);

    void deleteByWishlistIdAndVariantId(Long wishlistId, Long variantId);
}
