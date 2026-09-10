package com.bjdev.ecomercebase.repositories.discount;

import com.bjdev.ecomercebase.models.discount.DiscountItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DiscountItemRepository extends JpaRepository<DiscountItem, Long> {

    List<DiscountItem> findByDiscountId(Long discountId);

    void deleteByDiscountId(Long discountId);
}
