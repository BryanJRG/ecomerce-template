package com.bjdev.ecomercebase.controllers;

import com.bjdev.ecomercebase.dto.response.CategoryResponse;
import com.bjdev.ecomercebase.exception.CatalogException;
import com.bjdev.ecomercebase.services.interfaces.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Public storefront browsing (dropdowns/filters) — GET-only, permitted in SecurityConfig. */
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class PublicCategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> list() {
        return ResponseEntity.ok(categoryService.listCategories().stream()
                .filter(c -> Boolean.TRUE.equals(c.active()))
                .toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> get(@PathVariable Long id) {
        CategoryResponse category = categoryService.getCategory(id);
        if (!Boolean.TRUE.equals(category.active())) {
            throw CatalogException.categoryNotFound();
        }
        return ResponseEntity.ok(category);
    }
}
