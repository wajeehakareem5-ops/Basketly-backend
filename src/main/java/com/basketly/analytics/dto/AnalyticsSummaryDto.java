package com.basketly.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsSummaryDto {
    private BigDecimal totalDeliveredSpend;
    private BigDecimal totalCommittedSpend;
    private BigDecimal totalPlannedSpend;
    private BigDecimal totalDiscountSavings;
    private BigDecimal averageOrderValue;
    private int totalOrdersPlaced;
    private int totalPlannedItems;
    private double timeSavedHours;
}
