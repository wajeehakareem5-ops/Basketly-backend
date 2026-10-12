package com.basketly.budget.controller;

import com.basketly.analytics.service.AnalyticsService;
import com.basketly.budget.dto.BudgetSummaryDto;
import com.basketly.common.response.ApiResponse;
import com.basketly.security.CustomUserDetails;
import com.basketly.user.domain.UserSettings;
import com.basketly.user.repository.UserSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final AnalyticsService analyticsService;
    private final UserSettingsRepository userSettingsRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<BudgetSummaryDto>> getBudgets(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        BudgetSummaryDto summary = analyticsService.getBudgetSummary(userDetails.getUser());
        return ResponseEntity.ok(ApiResponse.ok(summary));
    }

    @PutMapping("/weekly")
    public ResponseEntity<ApiResponse<BudgetSummaryDto>> updateWeeklyBudget(
            @RequestBody Map<String, BigDecimal> body,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        BigDecimal newBudget = body.get("amount");
        if (newBudget != null && newBudget.compareTo(BigDecimal.ZERO) >= 0) {
            UserSettings settings = userSettingsRepository.findById(userDetails.getId())
                    .orElseGet(() -> UserSettings.builder().user(userDetails.getUser()).build());
            settings.setWeeklyBudget(newBudget);
            userSettingsRepository.save(settings);
        }
        return ResponseEntity.ok(ApiResponse.ok("Weekly budget updated", analyticsService.getBudgetSummary(userDetails.getUser())));
    }

    @PutMapping("/monthly")
    public ResponseEntity<ApiResponse<BudgetSummaryDto>> updateMonthlyBudget(
            @RequestBody Map<String, BigDecimal> body,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        BigDecimal newBudget = body.get("amount");
        if (newBudget != null && newBudget.compareTo(BigDecimal.ZERO) >= 0) {
            UserSettings settings = userSettingsRepository.findById(userDetails.getId())
                    .orElseGet(() -> UserSettings.builder().user(userDetails.getUser()).build());
            settings.setMonthlyBudget(newBudget);
            userSettingsRepository.save(settings);
        }
        return ResponseEntity.ok(ApiResponse.ok("Monthly budget updated", analyticsService.getBudgetSummary(userDetails.getUser())));
    }
}
