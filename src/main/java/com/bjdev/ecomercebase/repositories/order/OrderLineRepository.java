package com.bjdev.ecomercebase.repositories.order;

import com.bjdev.ecomercebase.models.order.OrderLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderLineRepository extends JpaRepository<OrderLine, Long> {

    List<OrderLine> findByOrderId(Long orderId);

    /** "Did this user ever buy this item" — used to mark a Review as a verified purchase. Any one matching line is enough; which order/line is irrelevant here. */
    Optional<OrderLine> findFirstByOrder_Client_UserIdAndVariant_Item_Id(Long userId, Long itemId);
}
