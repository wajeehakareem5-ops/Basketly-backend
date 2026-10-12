package com.basketly.cart.controller;

import com.basketly.cart.dto.AddToCartRequest;
import com.basketly.cart.dto.CartDto;
import com.basketly.cart.dto.UpdateCartItemRequest;
import com.basketly.cart.service.CartService;
import com.basketly.common.response.ApiResponse;
import com.basketly.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<ApiResponse<CartDto>> getCart(
            @RequestParam(required = false) String coupon,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        CartDto cart = cartService.getCart(userDetails.getUser(), coupon);
        return ResponseEntity.ok(ApiResponse.ok(cart));
    }

    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartDto>> addItem(
            @Valid @RequestBody AddToCartRequest request,
            @RequestParam(required = false) String coupon,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        CartDto cart = cartService.addItem(userDetails.getUser(), request, coupon);
        return ResponseEntity.ok(ApiResponse.ok("Item added to cart", cart));
    }

    @PatchMapping("/items/{id}")
    public ResponseEntity<ApiResponse<CartDto>> updateQuantity(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCartItemRequest request,
            @RequestParam(required = false) String coupon,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        CartDto cart = cartService.updateItemQuantity(userDetails.getUser(), id, request.getQuantity(), coupon);
        return ResponseEntity.ok(ApiResponse.ok("Cart item updated", cart));
    }

    @DeleteMapping("/items/{id}")
    public ResponseEntity<ApiResponse<CartDto>> removeItem(
            @PathVariable UUID id,
            @RequestParam(required = false) String coupon,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        CartDto cart = cartService.removeItem(userDetails.getUser(), id, coupon);
        return ResponseEntity.ok(ApiResponse.ok("Item removed from cart", cart));
    }

    @PostMapping("/coupons")
    public ResponseEntity<ApiResponse<CartDto>> applyCoupon(
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String couponCode = body.get("code");
        CartDto cart = cartService.getCart(userDetails.getUser(), couponCode);
        if (cart.getAppliedCouponCode() == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Invalid or non-applicable coupon code"));
        }
        return ResponseEntity.ok(ApiResponse.ok("Coupon applied successfully", cart));
    }

    @DeleteMapping("/coupons")
    public ResponseEntity<ApiResponse<CartDto>> removeCoupon(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        CartDto cart = cartService.getCart(userDetails.getUser(), null);
        return ResponseEntity.ok(ApiResponse.ok("Coupon removed", cart));
    }
}
