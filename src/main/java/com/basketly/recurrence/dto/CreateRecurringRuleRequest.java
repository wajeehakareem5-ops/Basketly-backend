package com.basketly.recurrence.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateRecurringRuleRequest {

    @NotNull(message = "Product ID is required")
    private UUID productId;

    @NotBlank(message = "Frequency is required (DAILY, WEEKLY, BIWEEKLY, SELECTED_WEEKDAY)")
    private String frequency;

    private DayOfWeek targetDayOfWeek;

    @Min(value = 1, message = "Quantity must be at least 1")
    @Builder.Default
    private int quantity = 1;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    private LocalDate endDate;
}
