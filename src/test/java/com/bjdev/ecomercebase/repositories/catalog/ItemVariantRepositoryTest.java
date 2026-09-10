package com.bjdev.ecomercebase.repositories.catalog;

import com.bjdev.ecomercebase.models.catalog.Category;
import com.bjdev.ecomercebase.models.catalog.Item;
import com.bjdev.ecomercebase.models.catalog.ItemVariant;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * reserveStock is the single most safety-critical query in the app — it's what stands between
 * two concurrent checkouts and a negative-stock oversell. @Transactional rolls each test back, so
 * we clear the persistence context before re-reading: the @Modifying UPDATE runs via direct JDBC
 * and never touches Hibernate's first-level cache, so a plain findById after it would otherwise
 * return the stale in-memory entity instead of what's actually in the row.
 * <p>
 * A true multi-threaded race test is deliberately NOT included here: it would need separate
 * connections/transactions outside this class's rollback wrapper (much more test infrastructure)
 * for marginal extra confidence over what's already proven below — that the query's
 * {@code WHERE v.stock >= :qty} clause, not any application-level lock, is what makes a second
 * reservation against exhausted stock fail.
 */
@SpringBootTest
@TestPropertySource(properties = "spring.docker.compose.skip.in-tests=false")
@Transactional
class ItemVariantRepositoryTest {

    @Autowired
    private ItemVariantRepository itemVariantRepository;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private EntityManager entityManager;

    @Test
    void reserveStock_withEnoughStock_decrementsAndReportsOneAffectedRow() {
        ItemVariant variant = createVariant(10);

        int affected = itemVariantRepository.reserveStock(variant.getId(), 4);

        assertThat(affected).isEqualTo(1);
        assertThat(freshStockOf(variant.getId())).isEqualTo(6);
    }

    @Test
    void reserveStock_withInsufficientStock_reportsZeroAffectedRowsAndLeavesStockUnchanged() {
        ItemVariant variant = createVariant(3);

        int affected = itemVariantRepository.reserveStock(variant.getId(), 4);

        assertThat(affected).isEqualTo(0);
        assertThat(freshStockOf(variant.getId())).isEqualTo(3);
    }

    @Test
    void reserveStock_secondReservationAfterStockIsExhausted_fails() {
        ItemVariant variant = createVariant(1);

        int first = itemVariantRepository.reserveStock(variant.getId(), 1);
        int second = itemVariantRepository.reserveStock(variant.getId(), 1);

        assertThat(first).isEqualTo(1);
        assertThat(second).isEqualTo(0);
        assertThat(freshStockOf(variant.getId())).isEqualTo(0);
    }

    @Test
    void releaseStock_addsBackTheGivenQuantity() {
        ItemVariant variant = createVariant(5);
        itemVariantRepository.reserveStock(variant.getId(), 5);

        itemVariantRepository.releaseStock(variant.getId(), 2);

        assertThat(freshStockOf(variant.getId())).isEqualTo(2);
    }

    private int freshStockOf(Long variantId) {
        entityManager.clear();
        return itemVariantRepository.findById(variantId).orElseThrow().getStock();
    }

    private ItemVariant createVariant(int stock) {
        String unique = UUID.randomUUID().toString();
        Category category = categoryRepository.save(Category.builder()
                .name("Test Category " + unique)
                .slug("test-category-" + unique)
                .build());
        Item item = itemRepository.save(Item.builder()
                .name("Test Item " + unique)
                .category(category)
                .build());
        return itemVariantRepository.save(ItemVariant.builder()
                .item(item)
                .sku("SKU-" + unique)
                .name("Default")
                .price(new BigDecimal("10.00"))
                .stock(stock)
                .build());
    }
}
