package com.basketly.suggestion.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuggestionDto {
    private String id; // unique key
    private String type; // FAVORITE, STAPLE, BUDGET, RECURRENCE
    private String title;
    private String description;
    private String explanation;
    private UUID productId;
    private String productName;
    private String actionType; // ADD_TO_PLAN, VIEW_BUDGET, REVIEW_RECURRENCE
    private boolean dismissed;
}
