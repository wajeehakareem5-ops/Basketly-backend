package com.basketly.planner.dto;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePlannedItemRequest {

    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    private String notes;

    private Boolean completed;

    private LocalDate planDate;
}
