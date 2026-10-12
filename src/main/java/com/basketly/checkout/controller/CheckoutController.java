package com.basketly.checkout.controller;

import com.basketly.checkout.dto.CheckoutRequest;
import com.basketly.checkout.service.CheckoutService;
import com.basketly.common.response.ApiResponse;
import com.basketly.order.dto.OrderDto;
import com.basketly.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/checkout")
@RequiredArgsConstructor
public class CheckoutController {

    private final CheckoutService checkoutService;

    @PostMapping
    public ResponseEntity<ApiResponse<OrderDto>> checkout(
            @Valid @RequestBody CheckoutRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        OrderDto order = checkoutService.checkout(userDetails.getUser(), request, idempotencyKey);
        return ResponseEntity.ok(ApiResponse.ok("Order placed successfully", order));
    }
}
