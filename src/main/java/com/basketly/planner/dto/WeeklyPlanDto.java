package com.basketly.planner.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyPlanDto {
    private UUID id;
    private LocalDate weekStartDate;
    private LocalDate weekEndDate;
    private List<DayPlanDto> days;
    private int totalItemCount;
    private BigDecimal weeklyTotalAmount;
    private BigDecimal weeklyBudget;
    private BigDecimal remainingBudget;
    private double budgetPercentageUsed;
}
