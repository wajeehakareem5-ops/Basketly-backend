package com.basketly.dashboard.controller;

import com.basketly.common.response.ApiResponse;
import com.basketly.dashboard.dto.DashboardSummaryDto;
import com.basketly.dashboard.service.DashboardService;
import com.basketly.order.domain.Order;
import com.basketly.order.dto.OrderDto;
import com.basketly.order.repository.OrderRepository;
import com.basketly.security.CustomUserDetails;
import com.basketly.suggestion.dto.SuggestionDto;
import com.basketly.suggestion.service.SuggestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final OrderRepository orderRepository;
    private final SuggestionService suggestionService;

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<DashboardSummaryDto>> getSummary(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        DashboardSummaryDto summary = dashboardService.getDashboardSummary(userDetails.getUser());
        return ResponseEntity.ok(ApiResponse.ok(summary));
    }

    @GetMapping("/upcoming-deliveries")
    public ResponseEntity<ApiResponse<List<OrderDto>>> getUpcomingDeliveries(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<Order> deliveries = orderRepository.findUpcomingDeliveries(userDetails.getUser(), LocalDate.now());
        List<OrderDto> dtos = deliveries.stream().map(OrderDto::fromEntity).toList();
        return ResponseEntity.ok(ApiResponse.ok(dtos));
    }

    @GetMapping("/suggestions")
    public ResponseEntity<ApiResponse<List<SuggestionDto>>> getSuggestions(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<SuggestionDto> suggestions = suggestionService.generateSuggestions(userDetails.getUser());
        return ResponseEntity.ok(ApiResponse.ok(suggestions));
    }

    @PostMapping("/suggestions/{key}/dismiss")
    public ResponseEntity<ApiResponse<Void>> dismissSuggestion(
            @PathVariable String key,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        suggestionService.dismissSuggestion(userDetails.getUser(), key);
        return ResponseEntity.ok(ApiResponse.ok("Suggestion dismissed", null));
    }

    @PostMapping("/suggestions/{key}/apply")
    public ResponseEntity<ApiResponse<Void>> applySuggestion(
            @PathVariable String key,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        suggestionService.applySuggestion(userDetails.getUser(), key);
        return ResponseEntity.ok(ApiResponse.ok("Suggestion applied to plan", null));
    }
}
