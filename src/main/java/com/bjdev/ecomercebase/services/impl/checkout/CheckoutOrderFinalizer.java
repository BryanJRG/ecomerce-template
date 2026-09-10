package com.bjdev.ecomercebase.services.impl.checkout;

import com.bjdev.ecomercebase.dto.response.CheckoutResult;
import com.bjdev.ecomercebase.events.OrderOversoldAdminAlertEvent;
import com.bjdev.ecomercebase.events.OrderOversoldCustomerNotificationEvent;
import com.bjdev.ecomercebase.events.OrderPaidEvent;
import com.bjdev.ecomercebase.models.cart.Cart;
import com.bjdev.ecomercebase.models.cart.CartLine;
import com.bjdev.ecomercebase.models.checkout.StockReservation;
import com.bjdev.ecomercebase.models.client.Client;
import com.bjdev.ecomercebase.models.enums.CartStatus;
import com.bjdev.ecomercebase.models.enums.OrderStatus;
import com.bjdev.ecomercebase.models.enums.PaymentStatus;
import com.bjdev.ecomercebase.models.enums.PaymentType;
import com.bjdev.ecomercebase.models.enums.SalesChannel;
import com.bjdev.ecomercebase.models.order.Order;
import com.bjdev.ecomercebase.models.order.OrderLine;
import com.bjdev.ecomercebase.models.order.OrderStatusHistory;
import com.bjdev.ecomercebase.models.payment.Payment;
import com.bjdev.ecomercebase.repositories.cart.CartRepository;
import com.bjdev.ecomercebase.repositories.catalog.ItemVariantRepository;
import com.bjdev.ecomercebase.repositories.order.OrderLineRepository;
import com.bjdev.ecomercebase.repositories.order.OrderRepository;
import com.bjdev.ecomercebase.repositories.order.OrderStatusHistoryRepository;
import com.bjdev.ecomercebase.repositories.payment.PaymentRepository;
import com.bjdev.ecomercebase.services.impl.discount.DiscountApplication;
import com.bjdev.ecomercebase.services.impl.discount.DiscountResolver;
import com.bjdev.ecomercebase.strategies.PaymentResult;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Kept as its own bean (rather than a private method on CheckoutServiceImpl) so its
 * {@code @Transactional} boundary actually applies — an internal self-invocation on the same bean
 * would silently bypass Spring's proxy-based transaction advice.
 */
@Component
@RequiredArgsConstructor
public class CheckoutOrderFinalizer {

    private final OrderRepository orderRepository;
    private final OrderLineRepository orderLineRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;
    private final PaymentRepository paymentRepository;
    private final CartRepository cartRepository;
    private final ItemVariantRepository itemVariantRepository;
    private final StockReservationCoordinator stockReservationCoordinator;
    private final ApplicationEventPublisher eventPublisher;
    private final DiscountResolver discountResolver;

    @Transactional
    public CheckoutResult finalizeApprovedOrder(Cart cart, List<CartLine> lines, List<StockReservation> reservations,
                                                 PaymentResult paymentResult, PaymentType paymentType, BigDecimal total,
                                                 Optional<DiscountApplication> discountApplication) {
        Order order = orderRepository.save(Order.builder()
                .client(cart.getClient())
                .status(OrderStatus.PAID)
                .total(total)
                .salesChannel(SalesChannel.ONLINE)
                .build());

        List<Long> oversoldVariantIds = new ArrayList<>();
        for (CartLine line : lines) {
            orderLineRepository.save(OrderLine.builder()
                    .order(order)
                    .variant(line.getVariant())
                    .quantity(line.getQuantity())
                    .priceAtPurchase(line.getPriceAtAddition())
                    .itemNameSnapshot(line.getVariant().getItem().getName())
                    .build());

            // Residual-oversell guard: our own reservation path (ItemVariantRepository.reserveStock's
            // conditional UPDATE) never lets stock go negative — this only fires if stock was
            // independently edited (e.g. a manual admin correction) after the reservation was made.
            itemVariantRepository.findById(line.getVariant().getId())
                    .filter(v -> v.getStock() < 0)
                    .ifPresent(v -> oversoldVariantIds.add(v.getId()));
        }

        orderStatusHistoryRepository.save(OrderStatusHistory.builder()
                .order(order)
                .status(OrderStatus.PAID)
                .note("Pago confirmado via " + paymentType)
                .build());

        LocalDateTime paidAt = LocalDateTime.now();
        paymentRepository.save(Payment.builder()
                .order(order)
                .type(paymentType)
                .provider(providerNameFor(paymentType))
                .providerTransactionId(paymentResult.providerTransactionId())
                .amount(total)
                .status(PaymentStatus.COMPLETED)
                .paidAt(paidAt)
                .build());

        cart.setStatus(CartStatus.CONVERTED);
        cartRepository.save(cart);

        stockReservationCoordinator.confirmReservations(reservations);

        eventPublisher.publishEvent(new OrderPaidEvent(order.getId(), total, paidAt));

        discountApplication.ifPresent(application -> discountResolver.redeem(application, cart.getClient(), order));

        if (!oversoldVariantIds.isEmpty()) {
            Client client = cart.getClient();
            eventPublisher.publishEvent(
                    new OrderOversoldCustomerNotificationEvent(order.getId(), client.getId(), client.getEmail()));
            eventPublisher.publishEvent(new OrderOversoldAdminAlertEvent(order.getId(), oversoldVariantIds));
        }

        return new CheckoutResult(order.getId(), order.getStatus(), PaymentStatus.COMPLETED, total,
                paymentResult.providerTransactionId(), discountApplication.map(DiscountApplication::amount).orElse(null));
    }

    private String providerNameFor(PaymentType type) {
        return switch (type) {
            case CARD -> "WOMPI";
            case GOOGLE_PAY -> "GOOGLE_PAY";
        };
    }
}
