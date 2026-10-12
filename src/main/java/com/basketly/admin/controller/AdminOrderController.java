package com.basketly.admin.controller;

import com.basketly.common.exception.ResourceNotFoundException;
import com.basketly.common.response.ApiResponse;
import com.basketly.order.domain.Order;
import com.basketly.order.domain.OrderStatus;
import com.basketly.order.dto.OrderDto;
import com.basketly.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/admin/orders")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderRepository orderRepository;

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<Page<OrderDto>>> getAllOrders(
            @RequestParam(required = false) OrderStatus status,
            @PageableDefault(size = 15, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<Order> page;
        if (status != null) {
            page = orderRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
        } else {
            page = orderRepository.findAllByOrderByCreatedAtDesc(pageable);
        }
        return ResponseEntity.ok(ApiResponse.ok(page.map(OrderDto::fromEntity)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderDto>> getOrderById(@PathVariable UUID id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
        return ResponseEntity.ok(ApiResponse.ok(OrderDto.fromEntity(order)));
    }

    @PatchMapping("/{id}/status")
    @Transactional
    public ResponseEntity<ApiResponse<OrderDto>> updateOrderStatus(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {
        String newStatusStr = body.get("status");
        if (newStatusStr == null || newStatusStr.isBlank()) {
            throw new IllegalArgumentException("Status cannot be empty");
        }
        OrderStatus newStatus = OrderStatus.valueOf(newStatusStr.toUpperCase());

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

        order.setStatus(newStatus);
        order = orderRepository.save(order);

        return ResponseEntity.ok(ApiResponse.ok("Order status updated to " + newStatus, OrderDto.fromEntity(order)));
    }
}
