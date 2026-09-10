package com.bjdev.ecomercebase.repositories.discount;

import com.bjdev.ecomercebase.models.discount.DiscountExcludedVariant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DiscountExcludedVariantRepository extends JpaRepository<DiscountExcludedVariant, Long> {

    List<DiscountExcludedVariant> findByDiscountId(Long discountId);

    void deleteByDiscountId(Long discountId);
}
