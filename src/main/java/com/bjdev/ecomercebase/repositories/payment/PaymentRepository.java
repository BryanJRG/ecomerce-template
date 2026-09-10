package com.bjdev.ecomercebase.repositories.payment;

import com.bjdev.ecomercebase.models.payment.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
