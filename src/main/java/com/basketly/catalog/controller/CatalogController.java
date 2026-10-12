package com.basketly.catalog.controller;

import com.basketly.catalog.dto.CategoryDto;
import com.basketly.catalog.dto.ProductDto;
import com.basketly.catalog.service.CatalogService;
import com.basketly.common.response.ApiResponse;
import com.basketly.security.CustomUserDetails;
import com.basketly.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class CatalogController {

    private final CatalogService catalogService;

    private User getUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return userDetails != null ? userDetails.getUser() : null;
    }

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<CategoryDto>>> getCategories() {
        return ResponseEntity.ok(ApiResponse.ok(catalogService.getCategories()));
    }

    @GetMapping("/products")
    public ResponseEntity<ApiResponse<Page<ProductDto>>> getProducts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false, defaultValue = "false") boolean availableOnly,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Page<ProductDto> products = catalogService.getProducts(
                category,
                query,
                minPrice,
                maxPrice,
                availableOnly,
                pageable,
                getUser(userDetails)
        );
        return ResponseEntity.ok(ApiResponse.ok(products));
    }

    @GetMapping("/products/search")
    public ResponseEntity<ApiResponse<Page<ProductDto>>> searchProducts(
            @RequestParam("q") String query,
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Page<ProductDto> products = catalogService.getProducts(
                null,
                query,
                null,
                null,
                false,
                pageable,
                getUser(userDetails)
        );
        return ResponseEntity.ok(ApiResponse.ok(products));
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<ApiResponse<ProductDto>> getProduct(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.ok(catalogService.getProductById(id, getUser(userDetails))));
    }

    @GetMapping("/products/{id}/related")
    public ResponseEntity<ApiResponse<List<ProductDto>>> getRelatedProducts(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.ok(catalogService.getRelatedProducts(id, getUser(userDetails))));
    }
}
