package com.bjdev.ecomercebase.repositories.refund;

import com.bjdev.ecomercebase.models.enums.RefundStatus;
import com.bjdev.ecomercebase.models.refund.RefundLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RefundLineRepository extends JpaRepository<RefundLine, Long> {

    List<RefundLine> findByRefundId(Long refundId);

    /** Guards against double-refunding the same order line while another refund covering it is pending/approved/completed. */
    boolean existsByOrderLineIdAndRefund_StatusIn(Long orderLineId, List<RefundStatus> statuses);
}
