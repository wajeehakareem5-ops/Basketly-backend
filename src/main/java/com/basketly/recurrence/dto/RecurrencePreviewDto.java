package com.basketly.recurrence.dto;

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
public class RecurrencePreviewDto {
    private List<LocalDate> projectedDates;
    private int totalOccurrences;
    private BigDecimal estimatedTotalCost;
}
