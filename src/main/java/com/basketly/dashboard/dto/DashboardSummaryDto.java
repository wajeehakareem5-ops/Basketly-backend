package com.basketly.dashboard.dto;

import com.basketly.order.dto.OrderDto;
import com.basketly.planner.dto.WeeklyPlanDto;
import com.basketly.suggestion.dto.SuggestionDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryDto {
    private String greeting;
    private String userName;
    private LocalDate weekStartDate;
    private LocalDate weekEndDate;

    // KPI Cards
    private BigDecimal estimatedWeeklySpending;
    private int plannedItemCount;
    private int scheduledOrdersCount;
    private int confirmedOrdersCount;
    private BigDecimal weeklyBudget;
    private BigDecimal remainingBudget;
    private double budgetPercentageUsed;

    // Time saved estimate
    private String timeSavedDisplay;
    private String timeSavedFormula;

    // Detailed planner strip and lists
    private WeeklyPlanDto weeklyPlan;
    private List<OrderDto> upcomingDeliveries;
    private List<SuggestionDto> suggestions;
}
