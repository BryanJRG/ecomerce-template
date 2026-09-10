package com.bjdev.ecomercebase.repositories.refund;

import com.bjdev.ecomercebase.models.refund.Refund;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RefundRepository extends JpaRepository<Refund, Long> {

    List<Refund> findByOrderId(Long orderId);

    /** Scoped by ownership (via the order's client) — same "wrong id and someone else's row return the same 404" reasoning as OrderRepository.findByIdAndClientId. */
    Optional<Refund> findByIdAndOrder_Client_Id(Long id, Long clientId);
}
