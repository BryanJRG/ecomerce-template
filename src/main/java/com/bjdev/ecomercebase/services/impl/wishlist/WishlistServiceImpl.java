package com.bjdev.ecomercebase.services.impl.wishlist;

import com.bjdev.ecomercebase.dto.response.WishlistLineResponse;
import com.bjdev.ecomercebase.exception.AuthException;
import com.bjdev.ecomercebase.exception.CatalogException;
import com.bjdev.ecomercebase.models.catalog.ItemVariant;
import com.bjdev.ecomercebase.models.user.User;
import com.bjdev.ecomercebase.models.wishlist.Wishlist;
import com.bjdev.ecomercebase.models.wishlist.WishlistLine;
import com.bjdev.ecomercebase.repositories.catalog.ItemVariantRepository;
import com.bjdev.ecomercebase.repositories.user.UserRepository;
import com.bjdev.ecomercebase.repositories.wishlist.WishlistLineRepository;
import com.bjdev.ecomercebase.repositories.wishlist.WishlistRepository;
import com.bjdev.ecomercebase.services.interfaces.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WishlistServiceImpl implements WishlistService {

    private final WishlistRepository wishlistRepository;
    private final WishlistLineRepository wishlistLineRepository;
    private final ItemVariantRepository itemVariantRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public List<WishlistLineResponse> addToWishlist(Long userId, Long variantId) {
        ItemVariant variant = itemVariantRepository.findById(variantId).orElseThrow(CatalogException::variantNotFound);
        Wishlist wishlist = getOrCreateWishlist(userId);

        if (wishlistLineRepository.findByWishlistIdAndVariantId(wishlist.getId(), variantId).isEmpty()) {
            wishlistLineRepository.save(WishlistLine.builder().wishlist(wishlist).variant(variant).build());
        }

        return listWishlist(userId);
    }

    @Override
    @Transactional
    public List<WishlistLineResponse> removeFromWishlist(Long userId, Long variantId) {
        Wishlist wishlist = getOrCreateWishlist(userId);
        wishlistLineRepository.deleteByWishlistIdAndVariantId(wishlist.getId(), variantId);
        return listWishlist(userId);
    }

    @Override
    public List<WishlistLineResponse> listWishlist(Long userId) {
        return wishlistRepository.findByUserId(userId)
                .map(w -> wishlistLineRepository.findByWishlistId(w.getId()).stream().map(this::toResponse).toList())
                .orElse(List.of());
    }

    private Wishlist getOrCreateWishlist(Long userId) {
        return wishlistRepository.findByUserId(userId).orElseGet(() -> {
            User user = userRepository.findById(userId).orElseThrow(AuthException::invalidCredentials);
            return wishlistRepository.save(Wishlist.builder().user(user).build());
        });
    }

    private WishlistLineResponse toResponse(WishlistLine line) {
        ItemVariant variant = line.getVariant();
        return new WishlistLineResponse(variant.getId(), variant.getItem().getName(), variant.getName(),
                variant.getPrice(), variant.getActive());
    }
}
