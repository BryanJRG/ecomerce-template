package com.bjdev.ecomercebase.repositories.order;

import com.bjdev.ecomercebase.models.order.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByClientIdOrderByCreatedAtDesc(Long clientId);

    /** Scoped by ownership so a wrong id and someone else's order return the exact same 404 — never let a caller probe which order ids exist. */
    Optional<Order> findByIdAndClientId(Long id, Long clientId);
}
