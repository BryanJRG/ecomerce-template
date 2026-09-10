package com.bjdev.ecomercebase.services.interfaces;

import com.bjdev.ecomercebase.dto.request.RefundCreateRequest;
import com.bjdev.ecomercebase.dto.response.RefundResponse;

import java.util.List;

public interface RefundService {

    /** Customer-initiated: creates the refund in REQUESTED status. Does not touch stock or the payment provider yet. */
    RefundResponse requestRefund(Long clientId, RefundCreateRequest request);

    RefundResponse getRefund(Long clientId, Long refundId);

    List<RefundResponse> listRefundsForOrder(Long clientId, Long orderId);

    // --- Admin actions ---

    RefundResponse approveRefund(Long refundId);

    RefundResponse rejectRefund(Long refundId);

    /**
     * Marks the refund COMPLETED, restores stock for every refunded line, and marks the parent
     * Order REFUNDED. Does NOT call out to the payment provider to reverse the charge — actually
     * moving the money back is an external, provider-specific step (done via the provider's own
     * dashboard/API) this template deliberately doesn't guess at; this only tracks that it happened.
     */
    RefundResponse completeRefund(Long refundId);

    List<RefundResponse> listAllRefunds();
}
