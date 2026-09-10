package com.bjdev.ecomercebase.services.impl.refund;

import com.bjdev.ecomercebase.dto.request.RefundCreateRequest;
import com.bjdev.ecomercebase.dto.request.RefundLineRequest;
import com.bjdev.ecomercebase.dto.response.RefundLineResponse;
import com.bjdev.ecomercebase.dto.response.RefundResponse;
import com.bjdev.ecomercebase.events.EntityAction;
import com.bjdev.ecomercebase.events.EntityChangedEvent;
import com.bjdev.ecomercebase.exception.NotFoundException;
import com.bjdev.ecomercebase.exception.RefundException;
import com.bjdev.ecomercebase.models.enums.OrderStatus;
import com.bjdev.ecomercebase.models.enums.RefundStatus;
import com.bjdev.ecomercebase.models.order.Order;
import com.bjdev.ecomercebase.models.order.OrderLine;
import com.bjdev.ecomercebase.models.refund.Refund;
import com.bjdev.ecomercebase.models.refund.RefundLine;
import com.bjdev.ecomercebase.models.refund.RefundStatusHistory;
import com.bjdev.ecomercebase.models.user.User;
import com.bjdev.ecomercebase.repositories.catalog.ItemVariantRepository;
import com.bjdev.ecomercebase.repositories.order.OrderLineRepository;
import com.bjdev.ecomercebase.repositories.order.OrderRepository;
import com.bjdev.ecomercebase.repositories.refund.RefundLineRepository;
import com.bjdev.ecomercebase.repositories.refund.RefundRepository;
import com.bjdev.ecomercebase.repositories.refund.RefundStatusHistoryRepository;
import com.bjdev.ecomercebase.security.CurrentUserProvider;
import com.bjdev.ecomercebase.services.impl.audit.AuditService;
import com.bjdev.ecomercebase.services.interfaces.RefundService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RefundServiceImpl implements RefundService {

    private static final String ENTITY_TYPE = "REFUND";
    private static final String TABLE_NAME = "refunds";
    private static final Set<OrderStatus> REFUNDABLE_ORDER_STATUSES =
            Set.of(OrderStatus.PAID, OrderStatus.PROCESSING, OrderStatus.SHIPPED, OrderStatus.DELIVERED);
    private static final List<RefundStatus> BLOCKING_REFUND_STATUSES =
            List.of(RefundStatus.REQUESTED, RefundStatus.APPROVED, RefundStatus.COMPLETED);

    private final RefundRepository refundRepository;
    private final RefundLineRepository refundLineRepository;
    private final RefundStatusHistoryRepository refundStatusHistoryRepository;
    private final OrderRepository orderRepository;
    private final OrderLineRepository orderLineRepository;
    private final ItemVariantRepository itemVariantRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final AuditService auditService;
    private final CurrentUserProvider currentUserProvider;

    @Override
    @Transactional
    public RefundResponse requestRefund(Long clientId, RefundCreateRequest request) {
        Order order = orderRepository.findByIdAndClientId(request.orderId(), clientId).orElseThrow(NotFoundException::order);
        if (!REFUNDABLE_ORDER_STATUSES.contains(order.getStatus())) {
            throw RefundException.orderNotRefundable();
        }

        Refund refund = refundRepository.save(Refund.builder()
                .order(order)
                .reason(request.reason())
                .totalAmount(BigDecimal.ZERO)
                .build());

        BigDecimal total = BigDecimal.ZERO;
        for (RefundLineRequest lineRequest : request.lines()) {
            OrderLine orderLine = orderLineRepository.findById(lineRequest.orderLineId())
                    .orElseThrow(RefundException::lineNotInOrder);
            if (!orderLine.getOrder().getId().equals(order.getId())) {
                throw RefundException.lineNotInOrder();
            }
            if (lineRequest.quantity() > orderLine.getQuantity()) {
                throw RefundException.quantityExceedsOrderLine();
            }
            if (refundLineRepository.existsByOrderLineIdAndRefund_StatusIn(orderLine.getId(), BLOCKING_REFUND_STATUSES)) {
                throw RefundException.lineAlreadyRefunded();
            }

            BigDecimal amount = orderLine.getPriceAtPurchase().multiply(BigDecimal.valueOf(lineRequest.quantity()));
            refundLineRepository.save(RefundLine.builder()
                    .refund(refund)
                    .orderLine(orderLine)
                    .quantity(lineRequest.quantity())
                    .amount(amount)
                    .build());
            total = total.add(amount);
        }

        refund.setTotalAmount(total);
        refund = refundRepository.save(refund);
        addHistory(refund, RefundStatus.REQUESTED);

        recordChange(EntityAction.CREATED, refund.getId(), "Refund #" + refund.getId() + " (order #" + order.getId() + ")");

        return toResponse(refund);
    }

    @Override
    public RefundResponse getRefund(Long clientId, Long refundId) {
        return toResponse(refundRepository.findByIdAndOrder_Client_Id(refundId, clientId)
                .orElseThrow(RefundException::refundNotFound));
    }

    @Override
    public List<RefundResponse> listRefundsForOrder(Long clientId, Long orderId) {
        orderRepository.findByIdAndClientId(orderId, clientId).orElseThrow(NotFoundException::order);
        return refundRepository.findByOrderId(orderId).stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public RefundResponse approveRefund(Long refundId) {
        Refund refund = getByIdOrThrow(refundId);
        if (refund.getStatus() != RefundStatus.REQUESTED) {
            throw RefundException.invalidStatusTransition();
        }
        refund.setStatus(RefundStatus.APPROVED);
        refund = refundRepository.save(refund);
        addHistory(refund, RefundStatus.APPROVED);

        recordChange(EntityAction.UPDATED, refund.getId(), "Refund #" + refund.getId());
        return toResponse(refund);
    }

    @Override
    @Transactional
    public RefundResponse rejectRefund(Long refundId) {
        Refund refund = getByIdOrThrow(refundId);
        if (refund.getStatus() != RefundStatus.REQUESTED) {
            throw RefundException.invalidStatusTransition();
        }
        refund.setStatus(RefundStatus.REJECTED);
        refund = refundRepository.save(refund);
        addHistory(refund, RefundStatus.REJECTED);

        recordChange(EntityAction.UPDATED, refund.getId(), "Refund #" + refund.getId());
        return toResponse(refund);
    }

    @Override
    @Transactional
    public RefundResponse completeRefund(Long refundId) {
        Refund refund = getByIdOrThrow(refundId);
        if (refund.getStatus() != RefundStatus.APPROVED) {
            throw RefundException.invalidStatusTransition();
        }

        for (RefundLine line : refundLineRepository.findByRefundId(refund.getId())) {
            itemVariantRepository.releaseStock(line.getOrderLine().getVariant().getId(), line.getQuantity());
        }

        refund.setStatus(RefundStatus.COMPLETED);
        refund = refundRepository.save(refund);
        addHistory(refund, RefundStatus.COMPLETED);

        Order order = refund.getOrder();
        order.setStatus(OrderStatus.REFUNDED);
        orderRepository.save(order);

        recordChange(EntityAction.UPDATED, refund.getId(), "Refund #" + refund.getId());
        return toResponse(refund);
    }

    @Override
    public List<RefundResponse> listAllRefunds() {
        return refundRepository.findAll().stream().map(this::toResponse).toList();
    }

    private void addHistory(Refund refund, RefundStatus status) {
        refundStatusHistoryRepository.save(RefundStatusHistory.builder().refund(refund).status(status).build());
    }

    private Refund getByIdOrThrow(Long refundId) {
        return refundRepository.findById(refundId).orElseThrow(RefundException::refundNotFound);
    }

    /** Same synchronous-audit + generic-event mechanism every admin-managed entity uses — see ItemServiceImpl.recordChange. */
    private void recordChange(EntityAction action, Long entityId, String entityLabel) {
        User actor = currentUserProvider.getCurrentUserOrNull();
        auditService.record(actor, ENTITY_TYPE + "_" + action, TABLE_NAME, entityId, null, null, null);
        eventPublisher.publishEvent(new EntityChangedEvent(
                ENTITY_TYPE, action, entityId, entityLabel, actor != null ? actor.getId() : null));
    }

    private RefundResponse toResponse(Refund refund) {
        List<RefundLineResponse> lines = refundLineRepository.findByRefundId(refund.getId()).stream()
                .map(l -> new RefundLineResponse(l.getOrderLine().getId(), l.getOrderLine().getItemNameSnapshot(),
                        l.getQuantity(), l.getAmount()))
                .toList();
        return new RefundResponse(refund.getId(), refund.getOrder().getId(), refund.getStatus(), refund.getReason(),
                refund.getTotalAmount(), refund.getCreatedAt(), lines);
    }
}
