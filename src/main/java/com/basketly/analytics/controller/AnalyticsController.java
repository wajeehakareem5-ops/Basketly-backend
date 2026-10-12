package com.basketly.analytics.controller;

import com.basketly.analytics.dto.AnalyticsSummaryDto;
import com.basketly.analytics.dto.CategorySpendingDto;
import com.basketly.analytics.dto.SpendingDataPointDto;
import com.basketly.analytics.service.AnalyticsService;
import com.basketly.common.response.ApiResponse;
import com.basketly.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<AnalyticsSummaryDto>> getSummary(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        AnalyticsSummaryDto summary = analyticsService.getAnalyticsSummary(userDetails.getUser());
        return ResponseEntity.ok(ApiResponse.ok(summary));
    }

    @GetMapping("/spending")
    public ResponseEntity<ApiResponse<List<SpendingDataPointDto>>> getSpending(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<SpendingDataPointDto> chart = analyticsService.getWeeklySpendingChart(userDetails.getUser());
        return ResponseEntity.ok(ApiResponse.ok(chart));
    }

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<CategorySpendingDto>>> getCategories(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<CategorySpendingDto> categories = analyticsService.getCategoryBreakdown(userDetails.getUser());
        return ResponseEntity.ok(ApiResponse.ok(categories));
    }
}
