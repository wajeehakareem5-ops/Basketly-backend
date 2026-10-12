package com.basketly.planner.dto;

import com.basketly.catalog.dto.ProductDto;
import com.basketly.planner.domain.PlannedItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlannedItemDto {
    private UUID id;
    private UUID weeklyPlanId;
    private ProductDto product;
    private LocalDate planDate;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal lineTotal;
    private String notes;
    private boolean completed;
    private UUID recurringRuleId;

    public static PlannedItemDto fromEntity(PlannedItem item) {
        return PlannedItemDto.builder()
                .id(item.getId())
                .weeklyPlanId(item.getWeeklyPlan().getId())
                .product(ProductDto.fromEntity(item.getProduct()))
                .planDate(item.getPlanDate())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .lineTotal(item.getLineTotal())
                .notes(item.getNotes())
                .completed(item.isCompleted())
                .recurringRuleId(item.getRecurringRule() != null ? item.getRecurringRule().getId() : null)
                .build();
    }
}
