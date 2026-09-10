package com.bjdev.ecomercebase.repositories.discount;

import com.bjdev.ecomercebase.models.discount.DiscountCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DiscountCategoryRepository extends JpaRepository<DiscountCategory, Long> {

    List<DiscountCategory> findByDiscountId(Long discountId);

    void deleteByDiscountId(Long discountId);
}
