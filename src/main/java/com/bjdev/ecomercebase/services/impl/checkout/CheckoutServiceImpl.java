package com.bjdev.ecomercebase.services.impl.checkout;

import com.bjdev.ecomercebase.dto.request.CheckoutRequest;
import com.bjdev.ecomercebase.dto.response.CheckoutResult;
import com.bjdev.ecomercebase.dto.response.CloneCartResult;
import com.bjdev.ecomercebase.exception.CheckoutException;
import com.bjdev.ecomercebase.models.cart.Cart;
import com.bjdev.ecomercebase.models.cart.CartLine;
import com.bjdev.ecomercebase.models.checkout.StockReservation;
import com.bjdev.ecomercebase.models.enums.CartStatus;
import com.bjdev.ecomercebase.repositories.cart.CartLineRepository;
import com.bjdev.ecomercebase.repositories.cart.CartRepository;
import com.bjdev.ecomercebase.services.impl.discount.DiscountApplication;
import com.bjdev.ecomercebase.services.impl.discount.DiscountResolver;
import com.bjdev.ecomercebase.services.interfaces.CartService;
import com.bjdev.ecomercebase.services.interfaces.CheckoutService;
import com.bjdev.ecomercebase.strategies.PaymentContext;
import com.bjdev.ecomercebase.strategies.PaymentResult;
import com.bjdev.ecomercebase.strategies.PaymentStrategyFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Orchestrates the four mandatory steps of checkout in order (see each step's comment below).
 * Deliberately NOT @Transactional at this level: step 2 calls out to an external payment
 * provider that may be slow (a Wompi 3DS challenge), and a DB transaction must never be held
 * open across that call. Each step below manages its own transaction instead (see
 * StockReservationCoordinator and CheckoutOrderFinalizer).
 */
@Service
@RequiredArgsConstructor
public class CheckoutServiceImpl implements CheckoutService {

    private final CartRepository cartRepository;
    private final CartLineRepository cartLineRepository;
    private final StockReservationCoordinator stockReservationCoordinator;
    private final PaymentStrategyFactory paymentStrategyFactory;
    private final CartService cartService;
    private final CheckoutOrderFinalizer orderFinalizer;
    private final DiscountResolver discountResolver;

    @Override
    public CheckoutResult checkout(Long clientId, CheckoutRequest request) {
        Cart cart = loadOwnedActiveCart(clientId, request.cartId());
        List<CartLine> lines = cartLineRepository.findByCartId(cart.getId());
        if (lines.isEmpty()) {
            throw CheckoutException.emptyCart();
        }

        BigDecimal subtotal = lines.stream()
                .map(l -> l.getPriceAtAddition().multiply(BigDecimal.valueOf(l.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Resolved (and validated, for an explicit code) BEFORE stock is touched — an invalid
        // discount code should fail fast, not after reserving stock the customer never pays for.
        Optional<DiscountApplication> discountApplication =
                discountResolver.resolve(lines, clientId, request.discountCode());
        BigDecimal total = discountApplication.map(a -> subtotal.subtract(a.amount())).orElse(subtotal);

        // Step 1 — reserve stock atomically, committed BEFORE the payment provider is ever contacted.
        List<StockReservation> reservations = stockReservationCoordinator.reserveForCart(cart, lines, request.idempotencyKey());

        // Step 2 — invoke the payment provider through the strategy factory, for the discounted total.
        PaymentContext context = new PaymentContext(clientId, cart.getId(), total, request.paymentType(),
                request.idempotencyKey(), request.paymentData());
        PaymentResult result = paymentStrategyFactory.resolve(request.paymentType()).process(context);

        if (!result.approved()) {
            // Step 3 — declined: compensate the reservation and hand the customer a fresh cart.
            return handleDeclinedPayment(cart, reservations, result);
        }

        // Step 4 — approved: convert the cart into Order + OrderLine + Payment(COMPLETED), and
        // record the redemption (if any) atomically alongside the order.
        return orderFinalizer.finalizeApprovedOrder(cart, lines, reservations, result, request.paymentType(),
                total, discountApplication);
    }

    private CheckoutResult handleDeclinedPayment(Cart cart, List<StockReservation> reservations, PaymentResult result) {
        stockReservationCoordinator.releaseAndPublish(reservations);

        CloneCartResult cloneResult = cartService.cloneCartOnFailedCheckout(cart.getId());
        List<String> priceChangeDetails = cloneResult.priceChanges().stream()
                .map(p -> p.variantName() + ": " + p.oldPrice() + " -> " + p.newPrice())
                .toList();

        throw CheckoutException.paymentDeclined(result.failureReason(), priceChangeDetails.isEmpty() ? null : priceChangeDetails);
    }

    /**
     * Looked up by id + ownership (not a repository derived-query on clientId) so a mismatch and a
     * missing cart return the exact same error — never let a caller probe which cart ids exist.
     */
    private Cart loadOwnedActiveCart(Long clientId, Long cartId) {
        Cart cart = cartRepository.findById(cartId).orElseThrow(CheckoutException::cartNotActive);
        if (!cart.getClient().getId().equals(clientId) || cart.getStatus() != CartStatus.ACTIVE) {
            throw CheckoutException.cartNotActive();
        }
        return cart;
    }
}
