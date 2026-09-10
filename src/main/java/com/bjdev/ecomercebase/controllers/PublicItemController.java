package com.bjdev.ecomercebase.controllers;

import com.bjdev.ecomercebase.dto.response.ItemResponse;
import com.bjdev.ecomercebase.dto.response.PageResponse;
import com.bjdev.ecomercebase.exception.CatalogException;
import com.bjdev.ecomercebase.services.interfaces.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

/** Public storefront browsing — GET-only, permitted in SecurityConfig regardless of the default anyRequest().authenticated(). */
@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class PublicItemController {

    private final ItemService itemService;

    @GetMapping
    public ResponseEntity<PageResponse<ItemResponse>> search(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            Pageable pageable) {
        return ResponseEntity.ok(PageResponse.from(
                itemService.searchItems(categoryId, brandId, name, minPrice, maxPrice, pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemResponse> get(@PathVariable Long id) {
        ItemResponse item = itemService.getItem(id);
        if (!Boolean.TRUE.equals(item.active())) {
            throw CatalogException.itemNotFound();
        }
        return ResponseEntity.ok(item);
    }
}
