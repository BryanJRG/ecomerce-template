package com.bjdev.ecomercebase.controllers;

import com.bjdev.ecomercebase.dto.request.BrandCreateRequest;
import com.bjdev.ecomercebase.dto.request.BrandUpdateRequest;
import com.bjdev.ecomercebase.dto.response.BrandResponse;
import com.bjdev.ecomercebase.services.interfaces.BrandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Protected by hasRole('ADMIN') + the Cloudflare tunnel filter (see SecurityConfig). */
@RestController
@RequestMapping("/api/admin/brands")
@RequiredArgsConstructor
public class BrandController {

    private final BrandService brandService;

    @PostMapping
    public ResponseEntity<BrandResponse> create(@Valid @RequestBody BrandCreateRequest request) {
        return ResponseEntity.ok(brandService.createBrand(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BrandResponse> update(@PathVariable Long id, @RequestBody BrandUpdateRequest request) {
        return ResponseEntity.ok(brandService.updateBrand(id, request));
    }

    @PostMapping("/{id}/deactivate")
    public ResponseEntity<BrandResponse> deactivate(@PathVariable Long id) {
        return ResponseEntity.ok(brandService.deactivateBrand(id));
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<BrandResponse> activate(@PathVariable Long id) {
        return ResponseEntity.ok(brandService.activateBrand(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BrandResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(brandService.getBrand(id));
    }

    @GetMapping
    public ResponseEntity<List<BrandResponse>> list() {
        return ResponseEntity.ok(brandService.listBrands());
    }
}
