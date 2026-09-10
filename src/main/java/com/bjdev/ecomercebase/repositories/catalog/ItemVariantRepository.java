package com.bjdev.ecomercebase.repositories.catalog;

import com.bjdev.ecomercebase.models.catalog.ItemVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ItemVariantRepository extends JpaRepository<ItemVariant, Long> {

    List<ItemVariant> findByItemId(Long itemId);

    /** Batch-fetch for a page of items, avoiding one query per item — see ItemServiceImpl.searchItems. */
    List<ItemVariant> findByItemIdIn(List<Long> itemIds);

    /**
     * Atomic, authoritative stock reservation: the row is only decremented if enough stock is
     * still available at the moment of the UPDATE, and the affected-row count tells the caller
     * whether it succeeded — no read-then-write race window. This is the ONLY place that should
     * ever be treated as the real reservation of stock (see CartServiceImpl.addToCart for the
     * separate, non-authoritative early check done at add-to-cart time).
     */
    @Modifying
    @Query("UPDATE ItemVariant v SET v.stock = v.stock - :qty WHERE v.id = :id AND v.stock >= :qty")
    int reserveStock(@Param("id") Long variantId, @Param("qty") Integer qty);

    /** Inverse of {@link #reserveStock}, used to compensate a failed/expired reservation. */
    @Modifying
    @Query("UPDATE ItemVariant v SET v.stock = v.stock + :qty WHERE v.id = :id")
    int releaseStock(@Param("id") Long variantId, @Param("qty") Integer qty);
}
