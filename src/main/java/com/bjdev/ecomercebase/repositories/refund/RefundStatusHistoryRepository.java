package com.bjdev.ecomercebase.repositories.refund;

import com.bjdev.ecomercebase.models.refund.RefundStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefundStatusHistoryRepository extends JpaRepository<RefundStatusHistory, Long> {
}
