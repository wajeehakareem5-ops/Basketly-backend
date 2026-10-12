package com.basketly.admin.controller;

import com.basketly.catalog.dto.ProductCreateUpdateRequest;
import com.basketly.catalog.dto.ProductDto;
import com.basketly.catalog.service.CatalogService;
import com.basketly.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/admin/products")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminProductController {

    private final CatalogService catalogService;

    @PostMapping
    public ResponseEntity<ApiResponse<ProductDto>> createProduct(
            @Valid @RequestBody ProductCreateUpdateRequest request) {
        ProductDto created = catalogService.createProduct(request);
        return ResponseEntity.ok(ApiResponse.ok("Product created successfully", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDto>> updateProduct(
            @PathVariable UUID id,
            @Valid @RequestBody ProductCreateUpdateRequest request) {
        ProductDto updated = catalogService.updateProduct(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Product updated successfully", updated));
    }

    @PatchMapping("/{id}/availability")
    public ResponseEntity<ApiResponse<ProductDto>> updateAvailability(
            @PathVariable UUID id,
            @RequestBody Map<String, Boolean> body) {
        boolean available = body.getOrDefault("isAvailable", true);
        ProductDto updated = catalogService.updateAvailability(id, available);
        return ResponseEntity.ok(ApiResponse.ok("Product availability updated", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable UUID id) {
        catalogService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.ok("Product deleted successfully", null));
    }
}
