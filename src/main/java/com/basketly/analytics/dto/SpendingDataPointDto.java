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
public class SpendingDataPointDto {
    private String label; // e.g. "Mon", "Tue" or "Week 1", "Week 2"
    private BigDecimal planned;
    private BigDecimal actual;
    private BigDecimal budget;
}
