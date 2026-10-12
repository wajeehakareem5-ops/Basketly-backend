package com.basketly.order.controller;

import com.basketly.common.response.ApiResponse;
import com.basketly.order.domain.OrderStatus;
import com.basketly.order.dto.OrderDto;
import com.basketly.order.service.OrderService;
import com.basketly.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<OrderDto>>> getOrders(
            @RequestParam(required = false) OrderStatus status,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Page<OrderDto> orders = orderService.getOrders(userDetails.getUser(), status, pageable);
        return ResponseEntity.ok(ApiResponse.ok(orders));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderDto>> getOrder(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        OrderDto order = orderService.getOrderById(userDetails.getUser(), id);
        return ResponseEntity.ok(ApiResponse.ok(order));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<OrderDto>> cancelOrder(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        OrderDto order = orderService.cancelOrder(userDetails.getUser(), id);
        return ResponseEntity.ok(ApiResponse.ok("Order cancelled successfully", order));
    }

    @PostMapping("/{id}/reorder")
    public ResponseEntity<ApiResponse<Void>> reorder(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        orderService.reorder(userDetails.getUser(), id);
        return ResponseEntity.ok(ApiResponse.ok("Items added to your cart", null));
    }
}
