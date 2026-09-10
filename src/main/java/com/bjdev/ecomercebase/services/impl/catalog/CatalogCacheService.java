package com.bjdev.ecomercebase.services.impl.catalog;

import com.bjdev.ecomercebase.dto.response.BrandResponse;
import com.bjdev.ecomercebase.dto.response.CategoryResponse;
import com.bjdev.ecomercebase.dto.response.ItemResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * KISS Redis cache for the catalog reads that matter: an Item is looked up by id (hot path for
 * product detail), while Brand/Category are cached as their full list (small, read-almost-always-
 * as-a-whole for dropdowns/filters) rather than per id. Filtered/paginated catalog listings are
 * deliberately NOT cached here — the combination cardinality makes the hit rate poor for the added
 * invalidation complexity. Same "cache is a safety net, not the source of truth" swallow-on-failure
 * style as IdempotentPaymentStrategy.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CatalogCacheService {

    private static final String ITEM_PREFIX = "catalog:item:";
    private static final String BRANDS_KEY = "catalog:brands:all";
    private static final String CATEGORIES_KEY = "catalog:categories:all";
    private static final Duration ITEM_TTL = Duration.ofMinutes(30);
    private static final Duration METADATA_TTL = Duration.ofHours(6);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public Optional<ItemResponse> getItem(Long id) {
        return read(ITEM_PREFIX + id, ItemResponse.class);
    }

    public void putItem(Long id, ItemResponse item) {
        write(ITEM_PREFIX + id, item, ITEM_TTL);
    }

    public void evictItem(Long id) {
        redisTemplate.delete(ITEM_PREFIX + id);
    }

    public Optional<List<BrandResponse>> getBrands() {
        return readList(BRANDS_KEY, BrandResponse[].class);
    }

    public void putBrands(List<BrandResponse> brands) {
        write(BRANDS_KEY, brands, METADATA_TTL);
    }

    public void evictBrands() {
        redisTemplate.delete(BRANDS_KEY);
    }

    public Optional<List<CategoryResponse>> getCategories() {
        return readList(CATEGORIES_KEY, CategoryResponse[].class);
    }

    public void putCategories(List<CategoryResponse> categories) {
        write(CATEGORIES_KEY, categories, METADATA_TTL);
    }

    public void evictCategories() {
        redisTemplate.delete(CATEGORIES_KEY);
    }

    private <T> Optional<T> read(String key, Class<T> type) {
        String raw = redisTemplate.opsForValue().get(key);
        if (raw == null) return Optional.empty();
        try {
            return Optional.of(objectMapper.readValue(raw, type));
        } catch (Exception e) {
            log.warn("Failed to deserialize cache entry for key={}, treating as cache miss", key, e);
            return Optional.empty();
        }
    }

    private <T> Optional<List<T>> readList(String key, Class<T[]> arrayType) {
        String raw = redisTemplate.opsForValue().get(key);
        if (raw == null) return Optional.empty();
        try {
            return Optional.of(Arrays.asList(objectMapper.readValue(raw, arrayType)));
        } catch (Exception e) {
            log.warn("Failed to deserialize cache entry for key={}, treating as cache miss", key, e);
            return Optional.empty();
        }
    }

    private void write(String key, Object value, Duration ttl) {
        try {
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(value), ttl);
        } catch (Exception e) {
            log.warn("Failed to write cache entry for key={}", key, e);
        }
    }
}
