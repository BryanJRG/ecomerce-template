package com.bjdev.ecomercebase.repositories.catalog;

import com.bjdev.ecomercebase.models.catalog.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {

    boolean existsByCategoryIdAndActiveTrue(Long categoryId);

    boolean existsByBrandIdAndActiveTrue(Long brandId);

    /** Used to build the blocking-items list on CatalogException.categoryInUse/brandInUse. */
    List<Item> findByCategoryIdAndActiveTrue(Long categoryId);

    List<Item> findByBrandIdAndActiveTrue(Long brandId);

    /**
     * Public catalog browsing: each filter is applied only when non-null — the classic "optional
     * JPQL parameter" idiom, simpler than a Specification/QueryDSL setup for a handful of flat
     * filters. Price lives on ItemVariant, not Item, so the price-range filter is an EXISTS
     * subquery ("at least one active variant falls in range") rather than a join — a join here
     * would multiply the Item row per matching variant and need an extra DISTINCT to undo it.
     */
    @Query("""
            SELECT i FROM Item i
            WHERE i.active = true
              AND (:categoryId IS NULL OR i.category.id = :categoryId)
              AND (:brandId IS NULL OR i.brand.id = :brandId)
              AND (:name IS NULL OR LOWER(i.name) LIKE LOWER(CONCAT('%', :name, '%')))
              AND ((:minPrice IS NULL AND :maxPrice IS NULL) OR EXISTS (
                    SELECT 1 FROM ItemVariant v
                    WHERE v.item = i AND v.active = true
                      AND (:minPrice IS NULL OR v.price >= :minPrice)
                      AND (:maxPrice IS NULL OR v.price <= :maxPrice)
              ))
            """)
    Page<Item> search(@Param("categoryId") Long categoryId, @Param("brandId") Long brandId,
                       @Param("name") String name, @Param("minPrice") BigDecimal minPrice,
                       @Param("maxPrice") BigDecimal maxPrice, Pageable pageable);
}
