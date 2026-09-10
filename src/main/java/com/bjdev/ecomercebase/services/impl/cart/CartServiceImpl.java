package com.bjdev.ecomercebase.services.impl.cart;

import com.bjdev.ecomercebase.dto.response.CartLineResponse;
import com.bjdev.ecomercebase.dto.response.CartResponse;
import com.bjdev.ecomercebase.dto.response.CloneCartResult;
import com.bjdev.ecomercebase.dto.response.PriceChangeNotice;
import com.bjdev.ecomercebase.exception.CartException;
import com.bjdev.ecomercebase.exception.CatalogException;
import com.bjdev.ecomercebase.models.cart.Cart;
import com.bjdev.ecomercebase.models.cart.CartLine;
import com.bjdev.ecomercebase.models.catalog.ItemVariant;
import com.bjdev.ecomercebase.models.client.Client;
import com.bjdev.ecomercebase.models.enums.CartStatus;
import com.bjdev.ecomercebase.repositories.cart.CartLineRepository;
import com.bjdev.ecomercebase.repositories.cart.CartRepository;
import com.bjdev.ecomercebase.repositories.catalog.ItemVariantRepository;
import com.bjdev.ecomercebase.repositories.client.ClientRepository;
import com.bjdev.ecomercebase.services.interfaces.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartLineRepository cartLineRepository;
    private final ItemVariantRepository itemVariantRepository;
    private final ClientRepository clientRepository;

    @Value("${app.cart.abandoned-retention-days:30}")
    private int abandonedRetentionDays;

    @Override
    @Transactional
    public CartResponse addToCart(Long clientId, Long variantId, Integer quantity) {
        ItemVariant variant = itemVariantRepository.findById(variantId)
                .orElseThrow(CatalogException::variantNotFound);
        if (!Boolean.TRUE.equals(variant.getActive())) {
            throw CartException.variantInactive();
        }

        Cart cart = getOrCreateActiveCart(clientId);
        CartLine line = cartLineRepository.findByCartIdAndVariantId(cart.getId(), variantId).orElse(null);
        int currentQty = line != null ? line.getQuantity() : 0;
        int requestedTotal = currentQty + quantity;

        // NOTE: this is an early UX check only ("don't let the customer add more than we currently
        // show as available") — it is NOT the authoritative reservation of stock and offers no
        // concurrency guarantee (two customers can pass this check for the same last unit at the
        // same time). The real, race-safe reservation happens exactly once, atomically, in
        // CheckoutServiceImpl via ItemVariantRepository.reserveStock at the moment of checkout.
        if (requestedTotal > variant.getStock()) {
            throw CartException.insufficientStock(variant.getName(), variant.getStock());
        }

        if (line != null) {
            line.setQuantity(requestedTotal);
        } else {
            line = CartLine.builder()
                    .cart(cart)
                    .variant(variant)
                    .quantity(quantity)
                    .priceAtAddition(variant.getPrice())
                    .build();
        }
        cartLineRepository.save(line);

        return toResponse(cart);
    }

    @Override
    public CartResponse getActiveCart(Long clientId) {
        Cart cart = cartRepository.findByClientIdAndStatus(clientId, CartStatus.ACTIVE)
                .orElseThrow(CartException::cartNotFound);
        return toResponse(cart);
    }

    @Override
    @Transactional
    public CloneCartResult cloneCartOnFailedCheckout(Long cartId) {
        Cart original = cartRepository.findById(cartId).orElseThrow(CartException::cartNotFound);
        original.setStatus(CartStatus.ABANDONED);
        cartRepository.save(original);

        Cart clone = cartRepository.save(Cart.builder()
                .client(original.getClient())
                .status(CartStatus.ACTIVE)
                .build());

        List<PriceChangeNotice> priceChanges = new ArrayList<>();
        for (CartLine oldLine : cartLineRepository.findByCartId(original.getId())) {
            ItemVariant variant = itemVariantRepository.findById(oldLine.getVariant().getId()).orElse(null);
            if (variant == null || !Boolean.TRUE.equals(variant.getActive())) {
                // No longer purchasable — dropped from the clone instead of carrying dead stock forward.
                continue;
            }

            if (variant.getPrice().compareTo(oldLine.getPriceAtAddition()) != 0) {
                priceChanges.add(new PriceChangeNotice(variant.getId(), variant.getName(),
                        oldLine.getPriceAtAddition(), variant.getPrice()));
            }

            cartLineRepository.save(CartLine.builder()
                    .cart(clone)
                    .variant(variant)
                    .quantity(oldLine.getQuantity())
                    .priceAtAddition(variant.getPrice())
                    .build());
        }

        return new CloneCartResult(toResponse(clone), priceChanges);
    }

    /**
     * Carts are hard-deleted — and only once ABANDONED and past the retention window: they
     * represent a transient, pre-purchase state with no accounting or audit implication, unlike
     * Orders/Payments, which are never deleted. The only other row ever hard-deleted in the
     * system is a resolved StockReservation (see StockReservationCleanupJob), for the same reason.
     */
    @Scheduled(cron = "${app.cart.purge-cron:0 0 3 1 * *}")
    @Transactional
    public void purgeAbandonedCarts() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(abandonedRetentionDays);
        List<Cart> abandoned = cartRepository.findByStatusAndUpdatedAtBefore(CartStatus.ABANDONED, threshold);
        if (abandoned.isEmpty()) {
            return;
        }
        for (Cart cart : abandoned) {
            cartLineRepository.deleteByCartId(cart.getId());
        }
        cartRepository.deleteAll(abandoned);
        log.info("Purged {} abandoned cart(s) older than {} day(s)", abandoned.size(), abandonedRetentionDays);
    }

    private Cart getOrCreateActiveCart(Long clientId) {
        return cartRepository.findByClientIdAndStatus(clientId, CartStatus.ACTIVE)
                .orElseGet(() -> {
                    Client client = clientRepository.findById(clientId).orElseThrow(CartException::cartNotFound);
                    return cartRepository.save(Cart.builder().client(client).status(CartStatus.ACTIVE).build());
                });
    }

    private CartResponse toResponse(Cart cart) {
        List<CartLineResponse> lines = cartLineRepository.findByCartId(cart.getId()).stream()
                .map(l -> new CartLineResponse(l.getId(), l.getVariant().getId(), l.getVariant().getItem().getName(),
                        l.getVariant().getName(), l.getQuantity(), l.getPriceAtAddition()))
                .toList();
        return new CartResponse(cart.getId(), cart.getClient().getId(), cart.getStatus(), lines);
    }
}
