package com.bjdev.ecomercebase.controllers;

import com.bjdev.ecomercebase.dto.request.CategoryCreateRequest;
import com.bjdev.ecomercebase.dto.request.CategoryUpdateRequest;
import com.bjdev.ecomercebase.dto.response.CategoryResponse;
import com.bjdev.ecomercebase.services.interfaces.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Protected by hasRole('ADMIN') + the Cloudflare tunnel filter (see SecurityConfig). */
@RestController
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<CategoryResponse> create(@Valid @RequestBody CategoryCreateRequest request) {
        return ResponseEntity.ok(categoryService.createCategory(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> update(@PathVariable Long id, @RequestBody CategoryUpdateRequest request) {
        return ResponseEntity.ok(categoryService.updateCategory(id, request));
    }

    @PostMapping("/{id}/deactivate")
    public ResponseEntity<CategoryResponse> deactivate(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.deactivateCategory(id));
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<CategoryResponse> activate(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.activateCategory(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getCategory(id));
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> list() {
        return ResponseEntity.ok(categoryService.listCategories());
    }
}
