package com.basketly.suggestion.service;

import com.basketly.catalog.domain.Product;
import com.basketly.catalog.repository.ProductRepository;
import com.basketly.favorite.domain.Favorite;
import com.basketly.favorite.repository.FavoriteRepository;
import com.basketly.planner.domain.PlannedItem;
import com.basketly.planner.dto.CreatePlannedItemRequest;
import com.basketly.planner.repository.PlannedItemRepository;
import com.basketly.planner.service.PlannerService;
import com.basketly.suggestion.domain.DismissedSuggestion;
import com.basketly.suggestion.dto.SuggestionDto;
import com.basketly.suggestion.repository.DismissedSuggestionRepository;
import com.basketly.user.domain.User;
import com.basketly.user.domain.UserSettings;
import com.basketly.user.repository.UserSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SuggestionService {

    private final DismissedSuggestionRepository dismissedSuggestionRepository;
    private final FavoriteRepository favoriteRepository;
    private final ProductRepository productRepository;
    private final PlannedItemRepository plannedItemRepository;
    private final UserSettingsRepository userSettingsRepository;
    private final PlannerService plannerService;

    @Transactional(readOnly = true)
    public List<SuggestionDto> generateSuggestions(User user) {
        List<SuggestionDto> suggestions = new ArrayList<>();
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(DayOfWeek.MONDAY);
        LocalDate weekEnd = weekStart.plusDays(6);

        // Fetch planned items for the current week
        List<PlannedItem> weekPlannedItems = plannedItemRepository.findByUserAndDateBetween(user.getId(), weekStart, weekEnd);
        Set<UUID> plannedProductIds = new HashSet<>();
        BigDecimal currentWeekPlannedSpend = BigDecimal.ZERO;
        for (PlannedItem item : weekPlannedItems) {
            plannedProductIds.add(item.getProduct().getId());
            currentWeekPlannedSpend = currentWeekPlannedSpend.add(item.getLineTotal());
        }

        // Rule 1: Check weekly budget limit
        UserSettings settings = userSettingsRepository.findById(user.getId()).orElse(null);
        if (settings != null && settings.getWeeklyBudget().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal budget = settings.getWeeklyBudget();
            double utilization = currentWeekPlannedSpend.divide(budget, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100)).doubleValue();

            String key = "BUDGET_EXHAUSTED_" + weekStart;
            if (utilization >= 85.0 && !dismissedSuggestionRepository.existsByUserAndSuggestionKey(user, key)) {
                suggestions.add(SuggestionDto.builder()
                        .id(key)
                        .type("BUDGET")
                        .title("Weekly Budget Alert (" + Math.round(utilization) + "% Used)")
                        .description("You have planned ₹" + currentWeekPlannedSpend + " out of your ₹" + budget + " weekly budget.")
                        .explanation("Explainable rule: Planned grocery spending has exceeded 85% of your configured threshold.")
                        .actionType("VIEW_BUDGET")
                        .dismissed(false)
                        .build());
            }
        }

        // Rule 2: Essential Staples Check (Milk, Eggs, Bread)
        List<Product> popularProducts = productRepository.findTop10ByAvailableTrueOrderByCreatedAtDesc();
        for (Product product : popularProducts) {
            String lowerName = product.getName().toLowerCase();
            boolean isStaple = lowerName.contains("milk") || lowerName.contains("eggs") || lowerName.contains("bread");
            if (isStaple && !plannedProductIds.contains(product.getId())) {
                String key = "STAPLE_MISSING_" + product.getId() + "_" + weekStart;
                if (!dismissedSuggestionRepository.existsByUserAndSuggestionKey(user, key)) {
                    suggestions.add(SuggestionDto.builder()
                            .id(key)
                            .type("STAPLE")
                            .title("Missing Kitchen Essential: " + product.getName())
                            .description("You haven't scheduled " + product.getName() + " for this week's meal plan.")
                            .explanation("Explainable rule: Essential household staple not detected in upcoming week's planner.")
                            .productId(product.getId())
                            .productName(product.getName())
                            .actionType("ADD_TO_PLAN")
                            .dismissed(false)
                            .build());
                }
            }
        }

        // Rule 3: Favorite item not planned
        List<Favorite> favorites = favoriteRepository.findByUserOrderByCreatedAtDesc(user);
        for (Favorite fav : favorites) {
            Product favProduct = fav.getProduct();
            if (!plannedProductIds.contains(favProduct.getId()) && favProduct.isAvailable()) {
                String key = "FAVORITE_UNPLANNED_" + favProduct.getId() + "_" + weekStart;
                if (!dismissedSuggestionRepository.existsByUserAndSuggestionKey(user, key)) {
                    suggestions.add(SuggestionDto.builder()
                            .id(key)
                            .type("FAVORITE")
                            .title("Plan Your Favorite: " + favProduct.getName())
                            .description("Add " + favProduct.getName() + " (₹" + favProduct.getPrice() + ") to this week's grocery list.")
                            .explanation("Explainable rule: Product is saved in your favorites but not added to your active planner.")
                            .productId(favProduct.getId())
                            .productName(favProduct.getName())
                            .actionType("ADD_TO_PLAN")
                            .dismissed(false)
                            .build());
                }
            }
        }

        return suggestions;
    }

    @Transactional
    public void dismissSuggestion(User user, String suggestionKey) {
        if (!dismissedSuggestionRepository.existsByUserAndSuggestionKey(user, suggestionKey)) {
            DismissedSuggestion ds = DismissedSuggestion.builder()
                    .user(user)
                    .suggestionKey(suggestionKey)
                    .build();
            dismissedSuggestionRepository.save(ds);
        }
    }

    @Transactional
    public void applySuggestion(User user, String suggestionKey) {
        List<SuggestionDto> suggestions = generateSuggestions(user);
        SuggestionDto target = suggestions.stream()
                .filter(s -> s.getId().equals(suggestionKey))
                .findFirst()
                .orElse(null);

        if (target != null && target.getProductId() != null) {
            LocalDate today = LocalDate.now();
            plannerService.addPlannedItem(user, CreatePlannedItemRequest.builder()
                    .productId(target.getProductId())
                    .planDate(today)
                    .quantity(1)
                    .notes("Added via Smart Suggestion")
                    .build());
        }

        dismissSuggestion(user, suggestionKey);
    }
}
