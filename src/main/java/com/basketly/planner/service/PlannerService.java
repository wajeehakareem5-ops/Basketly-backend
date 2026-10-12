package com.basketly.planner.service;

import com.basketly.catalog.domain.Product;
import com.basketly.catalog.repository.ProductRepository;
import com.basketly.common.exception.ResourceNotFoundException;
import com.basketly.planner.domain.PlannedItem;
import com.basketly.planner.domain.WeeklyPlan;
import com.basketly.planner.dto.*;
import com.basketly.planner.repository.PlannedItemRepository;
import com.basketly.planner.repository.WeeklyPlanRepository;
import com.basketly.recurrence.service.RecurrenceService;
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
import java.time.format.TextStyle;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PlannerService {

    private final WeeklyPlanRepository weeklyPlanRepository;
    private final PlannedItemRepository plannedItemRepository;
    private final ProductRepository productRepository;
    private final UserSettingsRepository userSettingsRepository;
    private final RecurrenceService recurrenceService;

    @Transactional
    public WeeklyPlan getOrCreateWeeklyPlan(User user, LocalDate weekStartDate) {
        return weeklyPlanRepository.findByUserAndWeekStartDate(user, weekStartDate)
                .orElseGet(() -> {
                    WeeklyPlan newPlan = WeeklyPlan.builder()
                            .user(user)
                            .weekStartDate(weekStartDate)
                            .build();
                    newPlan = weeklyPlanRepository.save(newPlan);
                    // Generate any applicable recurring items for this new week
                    recurrenceService.applyRecurrencesForWeek(user, newPlan);
                    return newPlan;
                });
    }

    @Transactional
    public WeeklyPlanDto getWeeklyPlanDto(User user, LocalDate weekStartDate) {
        WeeklyPlan weeklyPlan = getOrCreateWeeklyPlan(user, weekStartDate);

        List<PlannedItem> allItems = plannedItemRepository.findByWeeklyPlanOrderByPlanDateAscCreatedAtAsc(weeklyPlan);
        Map<LocalDate, List<PlannedItemDto>> itemsByDate = new HashMap<>();

        for (PlannedItem item : allItems) {
            itemsByDate.computeIfAbsent(item.getPlanDate(), k -> new ArrayList<>())
                    .add(PlannedItemDto.fromEntity(item));
        }

        List<DayPlanDto> dayPlans = new ArrayList<>();
        BigDecimal weeklyTotal = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        int totalItemsCount = 0;

        for (int i = 0; i < 7; i++) {
            LocalDate currentDate = weekStartDate.plusDays(i);
            List<PlannedItemDto> dayItems = itemsByDate.getOrDefault(currentDate, Collections.emptyList());

            BigDecimal dayTotal = dayItems.stream()
                    .map(PlannedItemDto::getLineTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .setScale(2, RoundingMode.HALF_UP);

            weeklyTotal = weeklyTotal.add(dayTotal);
            totalItemsCount += dayItems.size();

            DayOfWeek dow = currentDate.getDayOfWeek();
            dayPlans.add(DayPlanDto.builder()
                    .date(currentDate)
                    .dayOfWeek(dow)
                    .dayName(dow.getDisplayName(TextStyle.SHORT, Locale.ENGLISH))
                    .items(dayItems)
                    .itemCount(dayItems.size())
                    .totalAmount(dayTotal)
                    .build());
        }

        BigDecimal budget = userSettingsRepository.findById(user.getId())
                .map(UserSettings::getWeeklyBudget)
                .orElse(new BigDecimal("5000.00"))
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal remainingBudget = budget.subtract(weeklyTotal).setScale(2, RoundingMode.HALF_UP);
        double percentageUsed = 0.0;
        if (budget.compareTo(BigDecimal.ZERO) > 0) {
            percentageUsed = weeklyTotal.divide(budget, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue();
        }

        return WeeklyPlanDto.builder()
                .id(weeklyPlan.getId())
                .weekStartDate(weekStartDate)
                .weekEndDate(weekStartDate.plusDays(6))
                .days(dayPlans)
                .totalItemCount(totalItemsCount)
                .weeklyTotalAmount(weeklyTotal)
                .weeklyBudget(budget)
                .remainingBudget(remainingBudget)
                .budgetPercentageUsed(percentageUsed)
                .build();
    }

    @Transactional
    public PlannedItemDto addPlannedItem(User user, CreatePlannedItemRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + request.getProductId()));

        // Calculate week start for the plan date (assume Monday start by default)
        LocalDate weekStart = request.getPlanDate().with(DayOfWeek.MONDAY);
        WeeklyPlan plan = getOrCreateWeeklyPlan(user, weekStart);

        PlannedItem item = PlannedItem.builder()
                .weeklyPlan(plan)
                .product(product)
                .planDate(request.getPlanDate())
                .quantity(request.getQuantity())
                .unitPrice(product.getPrice())
                .notes(request.getNotes())
                .completed(false)
                .build();

        item = plannedItemRepository.save(item);
        return PlannedItemDto.fromEntity(item);
    }

    @Transactional
    public PlannedItemDto updatePlannedItem(User user, UUID itemId, UpdatePlannedItemRequest request) {
        PlannedItem item = plannedItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Planned item not found with id: " + itemId));

        if (!item.getWeeklyPlan().getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Planned item not found with id: " + itemId);
        }

        if (request.getQuantity() != null) {
            item.setQuantity(request.getQuantity());
        }
        if (request.getNotes() != null) {
            item.setNotes(request.getNotes());
        }
        if (request.getCompleted() != null) {
            item.setCompleted(request.getCompleted());
        }
        if (request.getPlanDate() != null) {
            item.setPlanDate(request.getPlanDate());
        }

        item = plannedItemRepository.save(item);
        return PlannedItemDto.fromEntity(item);
    }

    @Transactional
    public PlannedItemDto toggleCompleted(User user, UUID itemId) {
        PlannedItem item = plannedItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Planned item not found with id: " + itemId));

        if (!item.getWeeklyPlan().getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Planned item not found with id: " + itemId);
        }

        item.setCompleted(!item.isCompleted());
        item = plannedItemRepository.save(item);
        return PlannedItemDto.fromEntity(item);
    }

    @Transactional
    public void deletePlannedItem(User user, UUID itemId) {
        PlannedItem item = plannedItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Planned item not found with id: " + itemId));

        if (!item.getWeeklyPlan().getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Planned item not found with id: " + itemId);
        }

        plannedItemRepository.delete(item);
    }

    @Transactional
    public void copyDay(User user, LocalDate sourceDate, LocalDate targetDate) {
        LocalDate sourceWeekStart = sourceDate.with(DayOfWeek.MONDAY);
        WeeklyPlan sourcePlan = getOrCreateWeeklyPlan(user, sourceWeekStart);

        List<PlannedItem> sourceItems = plannedItemRepository.findByWeeklyPlanAndPlanDateOrderByCreatedAtAsc(sourcePlan, sourceDate);
        if (sourceItems.isEmpty()) {
            return;
        }

        LocalDate targetWeekStart = targetDate.with(DayOfWeek.MONDAY);
        WeeklyPlan targetPlan = getOrCreateWeeklyPlan(user, targetWeekStart);

        for (PlannedItem sourceItem : sourceItems) {
            PlannedItem copy = PlannedItem.builder()
                    .weeklyPlan(targetPlan)
                    .product(sourceItem.getProduct())
                    .planDate(targetDate)
                    .quantity(sourceItem.getQuantity())
                    .unitPrice(sourceItem.getUnitPrice())
                    .notes(sourceItem.getNotes())
                    .completed(false)
                    .build();
            plannedItemRepository.save(copy);
        }
    }

    @Transactional
    public void clearDay(User user, LocalDate date) {
        LocalDate weekStart = date.with(DayOfWeek.MONDAY);
        WeeklyPlan plan = getOrCreateWeeklyPlan(user, weekStart);
        plannedItemRepository.deleteByWeeklyPlanAndPlanDate(plan, date);
    }

    @Transactional
    public void copyWeek(User user, LocalDate sourceWeekStart, LocalDate targetWeekStart) {
        WeeklyPlan sourcePlan = getOrCreateWeeklyPlan(user, sourceWeekStart);
        List<PlannedItem> sourceItems = plannedItemRepository.findByWeeklyPlanOrderByPlanDateAscCreatedAtAsc(sourcePlan);

        if (sourceItems.isEmpty()) {
            return;
        }

        WeeklyPlan targetPlan = getOrCreateWeeklyPlan(user, targetWeekStart);
        long daysDiff = java.time.temporal.ChronoUnit.DAYS.between(sourceWeekStart, targetWeekStart);

        for (PlannedItem item : sourceItems) {
            LocalDate newDate = item.getPlanDate().plusDays(daysDiff);
            PlannedItem copy = PlannedItem.builder()
                    .weeklyPlan(targetPlan)
                    .product(item.getProduct())
                    .planDate(newDate)
                    .quantity(item.getQuantity())
                    .unitPrice(item.getUnitPrice())
                    .notes(item.getNotes())
                    .completed(false)
                    .build();
            plannedItemRepository.save(copy);
        }
    }
}
