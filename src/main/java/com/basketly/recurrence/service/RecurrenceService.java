package com.basketly.recurrence.service;

import com.basketly.catalog.domain.Product;
import com.basketly.catalog.repository.ProductRepository;
import com.basketly.common.exception.ResourceNotFoundException;
import com.basketly.planner.domain.PlannedItem;
import com.basketly.planner.domain.WeeklyPlan;
import com.basketly.planner.repository.PlannedItemRepository;
import com.basketly.recurrence.domain.RecurringRule;
import com.basketly.recurrence.dto.CreateRecurringRuleRequest;
import com.basketly.recurrence.dto.RecurrencePreviewDto;
import com.basketly.recurrence.repository.RecurringRuleRepository;
import com.basketly.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecurrenceService {

    private final RecurringRuleRepository recurringRuleRepository;
    private final ProductRepository productRepository;
    private final PlannedItemRepository plannedItemRepository;

    @Transactional
    public RecurringRule createRecurringRule(User user, CreateRecurringRuleRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + request.getProductId()));

        RecurringRule rule = RecurringRule.builder()
                .user(user)
                .product(product)
                .frequency(request.getFrequency().toUpperCase())
                .targetDayOfWeek(request.getTargetDayOfWeek() != null ? request.getTargetDayOfWeek() : request.getStartDate().getDayOfWeek())
                .quantity(request.getQuantity())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .active(true)
                .build();

        return recurringRuleRepository.save(rule);
    }

    public RecurrencePreviewDto previewRecurrence(CreateRecurringRuleRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + request.getProductId()));

        LocalDate start = request.getStartDate();
        LocalDate end = request.getEndDate() != null ? request.getEndDate() : start.plusWeeks(8);
        DayOfWeek targetDay = request.getTargetDayOfWeek() != null ? request.getTargetDayOfWeek() : start.getDayOfWeek();

        List<LocalDate> dates = generateDates(request.getFrequency().toUpperCase(), targetDay, start, end);
        BigDecimal estimatedCost = product.getPrice()
                .multiply(BigDecimal.valueOf(request.getQuantity()))
                .multiply(BigDecimal.valueOf(dates.size()))
                .setScale(2, RoundingMode.HALF_UP);

        return RecurrencePreviewDto.builder()
                .projectedDates(dates)
                .totalOccurrences(dates.size())
                .estimatedTotalCost(estimatedCost)
                .build();
    }

    @Transactional
    public void applyRecurrencesForWeek(User user, WeeklyPlan weeklyPlan) {
        List<RecurringRule> rules = recurringRuleRepository.findByUserAndActiveTrue(user);
        if (rules.isEmpty()) {
            return;
        }

        LocalDate weekStart = weeklyPlan.getWeekStartDate();
        LocalDate weekEnd = weekStart.plusDays(6);

        // Fetch existing items for this week to guarantee idempotency
        List<PlannedItem> existingItems = plannedItemRepository.findByWeeklyPlanOrderByPlanDateAscCreatedAtAsc(weeklyPlan);
        Set<String> existingKeys = existingItems.stream()
                .map(i -> i.getProduct().getId() + "_" + i.getPlanDate().toString())
                .collect(Collectors.toSet());

        for (RecurringRule rule : rules) {
            if (rule.getEndDate() != null && rule.getEndDate().isBefore(weekStart)) {
                continue;
            }
            if (rule.getStartDate().isAfter(weekEnd)) {
                continue;
            }

            LocalDate effectiveStart = rule.getStartDate().isAfter(weekStart) ? rule.getStartDate() : weekStart;
            LocalDate effectiveEnd = (rule.getEndDate() != null && rule.getEndDate().isBefore(weekEnd)) ? rule.getEndDate() : weekEnd;

            List<LocalDate> recurrenceDates = generateDates(rule.getFrequency(), rule.getTargetDayOfWeek(), effectiveStart, effectiveEnd);

            for (LocalDate date : recurrenceDates) {
                String key = rule.getProduct().getId() + "_" + date.toString();
                if (!existingKeys.contains(key)) {
                    PlannedItem item = PlannedItem.builder()
                            .weeklyPlan(weeklyPlan)
                            .product(rule.getProduct())
                            .planDate(date)
                            .quantity(rule.getQuantity())
                            .unitPrice(rule.getProduct().getPrice())
                            .notes("Recurring item (" + rule.getFrequency().toLowerCase() + ")")
                            .completed(false)
                            .recurringRule(rule)
                            .build();
                    plannedItemRepository.save(item);
                    existingKeys.add(key);
                }
            }
        }
    }

    private List<LocalDate> generateDates(String frequency, DayOfWeek targetDay, LocalDate start, LocalDate end) {
        List<LocalDate> dates = new ArrayList<>();
        LocalDate curr = start;

        switch (frequency) {
            case "DAILY" -> {
                while (!curr.isAfter(end)) {
                    dates.add(curr);
                    curr = curr.plusDays(1);
                }
            }
            case "WEEKLY", "SELECTED_WEEKDAY" -> {
                while (curr.getDayOfWeek() != targetDay && !curr.isAfter(end)) {
                    curr = curr.plusDays(1);
                }
                while (!curr.isAfter(end)) {
                    dates.add(curr);
                    curr = curr.plusWeeks(1);
                }
            }
            case "BIWEEKLY" -> {
                while (curr.getDayOfWeek() != targetDay && !curr.isAfter(end)) {
                    curr = curr.plusDays(1);
                }
                while (!curr.isAfter(end)) {
                    dates.add(curr);
                    curr = curr.plusWeeks(2);
                }
            }
            default -> {
                while (!curr.isAfter(end)) {
                    dates.add(curr);
                    curr = curr.plusDays(1);
                }
            }
        }
        return dates;
    }
}
