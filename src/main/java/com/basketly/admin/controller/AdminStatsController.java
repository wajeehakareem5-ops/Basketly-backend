package com.basketly.admin.controller;

import com.basketly.catalog.repository.ProductRepository;
import com.basketly.common.response.ApiResponse;
import com.basketly.order.domain.OrderStatus;
import com.basketly.order.dto.OrderDto;
import com.basketly.order.repository.OrderRepository;
import com.basketly.user.repository.UserRepository;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/admin/stats")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminStatsController {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Data
    @Builder
    public static class AdminDashboardStatsDto {
        private long totalOrders;
        private BigDecimal totalRevenue;
        private long totalUsers;
        private long totalProducts;
        private long activeProducts;
        private long pendingOrders;
        private long lowStockProducts;
        private List<OrderDto> recentOrders;
    }

    @GetMapping
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<AdminDashboardStatsDto>> getAdminStats() {
        long totalOrders = orderRepository.countTotalOrders();
        BigDecimal totalRevenue = orderRepository.sumTotalRevenue();
        long totalUsers = userRepository.count();
        long totalProducts = productRepository.count();
        long activeProducts = productRepository.countByAvailableTrue();
        long pendingOrders = orderRepository.countByStatusIn(List.of(OrderStatus.PENDING, OrderStatus.CONFIRMED, OrderStatus.PREPARING));
        long lowStockProducts = productRepository.countByStockQuantityLessThanEqual(10);

        List<OrderDto> recentOrders = orderRepository.findAllByOrderByCreatedAtDesc(
                PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt"))
        ).getContent().stream().map(OrderDto::fromEntity).toList();

        AdminDashboardStatsDto stats = AdminDashboardStatsDto.builder()
                .totalOrders(totalOrders)
                .totalRevenue(totalRevenue)
                .totalUsers(totalUsers)
                .totalProducts(totalProducts)
                .activeProducts(activeProducts)
                .pendingOrders(pendingOrders)
                .lowStockProducts(lowStockProducts)
                .recentOrders(recentOrders)
                .build();

        return ResponseEntity.ok(ApiResponse.ok(stats));
    }
}
