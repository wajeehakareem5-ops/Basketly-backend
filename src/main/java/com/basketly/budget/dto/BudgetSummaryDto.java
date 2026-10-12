package com.basketly.budget.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetSummaryDto {
    private BigDecimal weeklyBudget;
    private BigDecimal monthlyBudget;
    private BigDecimal weeklyPlannedSpending;
    private BigDecimal weeklyCommittedSpending;
    private BigDecimal weeklyActualSpending;
    private BigDecimal weeklyRemainingBudget;
    private double weeklyBudgetUtilization;
    private boolean weeklyBudgetAlert;

    private BigDecimal monthlyActualSpending;
    private BigDecimal monthlyRemainingBudget;
    private double monthlyBudgetUtilization;
    private boolean monthlyBudgetAlert;
}
