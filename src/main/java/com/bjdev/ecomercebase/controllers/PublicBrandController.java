package com.bjdev.ecomercebase.controllers;

import com.bjdev.ecomercebase.dto.response.BrandResponse;
import com.bjdev.ecomercebase.exception.CatalogException;
import com.bjdev.ecomercebase.services.interfaces.BrandService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Public storefront browsing (dropdowns/filters) — GET-only, permitted in SecurityConfig. */
@RestController
@RequestMapping("/api/brands")
@RequiredArgsConstructor
public class PublicBrandController {

    private final BrandService brandService;

    @GetMapping
    public ResponseEntity<List<BrandResponse>> list() {
        return ResponseEntity.ok(brandService.listBrands().stream()
                .filter(b -> Boolean.TRUE.equals(b.active()))
                .toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BrandResponse> get(@PathVariable Long id) {
        BrandResponse brand = brandService.getBrand(id);
        if (!Boolean.TRUE.equals(brand.active())) {
            throw CatalogException.brandNotFound();
        }
        return ResponseEntity.ok(brand);
    }
}
