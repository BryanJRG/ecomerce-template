package com.bjdev.ecomercebase.services.impl.catalog;

import com.bjdev.ecomercebase.events.EntityChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Generic cache invalidation for every entity that publishes EntityChangedEvent. Deliberately NOT
 * @Async, unlike EntityChangeNotificationListener: eviction must complete before the response
 * returns, or a concurrent read racing the same commit could repopulate the cache with stale data.
 * Swallows failures anyway — this runs AFTER_COMMIT, so there's no transaction left to roll back;
 * worst case the cache stays stale until its TTL expires.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EntityCacheEvictionListener {

    private final CatalogCacheService catalogCacheService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onEntityChanged(EntityChangedEvent event) {
        try {
            switch (event.entityType()) {
                case "ITEM" -> catalogCacheService.evictItem(event.entityId());
                case "BRAND" -> catalogCacheService.evictBrands();
                case "CATEGORY" -> catalogCacheService.evictCategories();
                default -> {
                    // No cache defined for this entity type (e.g. DISCOUNT) — nothing to evict.
                }
            }
        } catch (Exception e) {
            log.error("Failed to evict cache for {} {}: {}", event.entityType(), event.entityId(), e.getMessage(), e);
        }
    }
}
