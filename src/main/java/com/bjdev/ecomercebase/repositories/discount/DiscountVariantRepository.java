package com.bjdev.ecomercebase.repositories.discount;

import com.bjdev.ecomercebase.models.discount.DiscountVariant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DiscountVariantRepository extends JpaRepository<DiscountVariant, Long> {

    List<DiscountVariant> findByDiscountId(Long discountId);

    void deleteByDiscountId(Long discountId);
}
