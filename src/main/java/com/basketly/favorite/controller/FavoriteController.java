package com.basketly.favorite.controller;

import com.basketly.catalog.dto.ProductDto;
import com.basketly.common.response.ApiResponse;
import com.basketly.favorite.service.FavoriteService;
import com.basketly.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductDto>>> getFavorites(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<ProductDto> favorites = favoriteService.getFavorites(userDetails.getUser());
        return ResponseEntity.ok(ApiResponse.ok(favorites));
    }

    @PostMapping("/{productId}")
    public ResponseEntity<ApiResponse<Void>> addFavorite(
            @PathVariable UUID productId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        favoriteService.addFavorite(userDetails.getUser(), productId);
        return ResponseEntity.ok(ApiResponse.ok("Added to favorites", null));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponse<Void>> removeFavorite(
            @PathVariable UUID productId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        favoriteService.removeFavorite(userDetails.getUser(), productId);
        return ResponseEntity.ok(ApiResponse.ok("Removed from favorites", null));
    }
}
