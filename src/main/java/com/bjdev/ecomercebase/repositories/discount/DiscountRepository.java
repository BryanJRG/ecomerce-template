package com.bjdev.ecomercebase.repositories.discount;

import com.bjdev.ecomercebase.models.discount.Discount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DiscountRepository extends JpaRepository<Discount, Long> {

    boolean existsByCode(String code);

    Optional<Discount> findByCodeAndActiveTrue(String code);

    /** Candidates for auto-apply at checkout — no code, currently active and in date range. Further eligibility (min purchase, usage caps, targeting) is checked in DiscountResolver. */
    @Query("SELECT d FROM Discount d WHERE d.code IS NULL AND d.active = true AND d.startDate <= :now AND d.endDate >= :now")
    List<Discount> findAutoApplyCandidates(@Param("now") LocalDateTime now);

    /**
     * Atomic, authoritative usage-cap enforcement: same "conditional UPDATE checked by
     * affected-row count" idiom as ItemVariantRepository.reserveStock. A null maxUsesTotal means
     * unlimited, so the row is always incremented in that case.
     */
    @Modifying
    @Query("UPDATE Discount d SET d.usedCount = d.usedCount + 1 WHERE d.id = :id AND (d.maxUsesTotal IS NULL OR d.usedCount < d.maxUsesTotal)")
    int redeem(@Param("id") Long id);
}
