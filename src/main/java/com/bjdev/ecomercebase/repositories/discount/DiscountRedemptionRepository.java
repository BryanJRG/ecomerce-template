package com.bjdev.ecomercebase.repositories.discount;

import com.bjdev.ecomercebase.models.discount.DiscountRedemption;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiscountRedemptionRepository extends JpaRepository<DiscountRedemption, Long> {

    long countByDiscountIdAndClientId(Long discountId, Long clientId);
}
