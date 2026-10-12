package com.basketly.analytics.service;

import com.basketly.analytics.dto.AnalyticsSummaryDto;
import com.basketly.analytics.dto.CategorySpendingDto;
import com.basketly.analytics.dto.SpendingDataPointDto;
import com.basketly.budget.dto.BudgetSummaryDto;
import com.basketly.catalog.domain.Category;
import com.basketly.order.domain.Order;
import com.basketly.order.domain.OrderItem;
import com.basketly.order.domain.OrderStatus;
import com.basketly.order.repository.OrderRepository;
import com.basketly.planner.domain.PlannedItem;
import com.basketly.planner.repository.PlannedItemRepository;
import com.basketly.user.domain.User;
import com.basketly.user.domain.UserSettings;
import com.basketly.user.repository.UserSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final OrderRepository orderRepository;
    private final PlannedItemRepository plannedItemRepository;
    private final UserSettingsRepository userSettingsRepository;

    @Transactional(readOnly = true)
    public BudgetSummaryDto getBudgetSummary(User user) {
        UserSettings settings = userSettingsRepository.findById(user.getId())
                .orElseGet(() -> UserSettings.builder().build());

        BigDecimal weeklyBudget = settings.getWeeklyBudget().setScale(2, RoundingMode.HALF_UP);
        BigDecimal monthlyBudget = settings.getMonthlyBudget().setScale(2, RoundingMode.HALF_UP);

        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(DayOfWeek.MONDAY);
        LocalDate weekEnd = weekStart.plusDays(6);
        Instant weekStartInstant = weekStart.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant monthStartInstant = today.withDayOfMonth(1).atStartOfDay().toInstant(ZoneOffset.UTC);

        // 1. Planned spending for current week
        List<PlannedItem> weekPlannedItems = plannedItemRepository.findByUserAndDateBetween(user.getId(), weekStart, weekEnd);
        BigDecimal weeklyPlanned = weekPlannedItems.stream()
                .map(PlannedItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        // 2. Orders for the week & month
        List<Order> weekOrders = orderRepository.findByUserAndCreatedAtAfter(user, weekStartInstant);
        List<Order> monthOrders = orderRepository.findByUserAndCreatedAtAfter(user, monthStartInstant);

        // Committed spending: Confirmed, not yet delivered
        BigDecimal weeklyCommitted = weekOrders.stream()
                .filter(o -> o.getStatus() == OrderStatus.CONFIRMED ||
                             o.getStatus() == OrderStatus.PREPARING ||
                             o.getStatus() == OrderStatus.READY_FOR_DELIVERY ||
                             o.getStatus() == OrderStatus.OUT_FOR_DELIVERY)
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        // Actual spending: Delivered orders only
        BigDecimal weeklyActual = weekOrders.stream()
                .filter(o -> o.getStatus() == OrderStatus.DELIVERED)
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal monthlyActual = monthOrders.stream()
                .filter(o -> o.getStatus() == OrderStatus.DELIVERED)
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        // Remaining budget based on actual + committed
        BigDecimal totalWeeklySpendMeasure = weeklyActual.add(weeklyCommitted).add(weeklyPlanned);
        BigDecimal weeklyRemaining = weeklyBudget.subtract(totalWeeklySpendMeasure).setScale(2, RoundingMode.HALF_UP);

        double weeklyUtilization = 0.0;
        if (weeklyBudget.compareTo(BigDecimal.ZERO) > 0) {
            weeklyUtilization = totalWeeklySpendMeasure.divide(weeklyBudget, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100)).doubleValue();
        }

        BigDecimal monthlyRemaining = monthlyBudget.subtract(monthlyActual).setScale(2, RoundingMode.HALF_UP);
        double monthlyUtilization = 0.0;
        if (monthlyBudget.compareTo(BigDecimal.ZERO) > 0) {
            monthlyUtilization = monthlyActual.divide(monthlyBudget, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100)).doubleValue();
        }

        return BudgetSummaryDto.builder()
                .weeklyBudget(weeklyBudget)
                .monthlyBudget(monthlyBudget)
                .weeklyPlannedSpending(weeklyPlanned)
                .weeklyCommittedSpending(weeklyCommitted)
                .weeklyActualSpending(weeklyActual)
                .weeklyRemainingBudget(weeklyRemaining)
                .weeklyBudgetUtilization(weeklyUtilization)
                .weeklyBudgetAlert(weeklyUtilization >= 80.0)
                .monthlyActualSpending(monthlyActual)
                .monthlyRemainingBudget(monthlyRemaining)
                .monthlyBudgetUtilization(monthlyUtilization)
                .monthlyBudgetAlert(monthlyUtilization >= 80.0)
                .build();
    }

    @Transactional(readOnly = true)
    public AnalyticsSummaryDto getAnalyticsSummary(User user) {
        Instant thirtyDaysAgo = Instant.now().minus(30, ChronoUnit.DAYS);
        List<Order> recentOrders = orderRepository.findByUserAndCreatedAtAfter(user, thirtyDaysAgo);

        BigDecimal totalDelivered = BigDecimal.ZERO;
        BigDecimal totalCommitted = BigDecimal.ZERO;
        BigDecimal totalDiscounts = BigDecimal.ZERO;
        int deliveredCount = 0;

        for (Order o : recentOrders) {
            if (o.getStatus() == OrderStatus.DELIVERED) {
                totalDelivered = totalDelivered.add(o.getTotalAmount());
                totalDiscounts = totalDiscounts.add(o.getDiscountAmount());
                deliveredCount++;
            } else if (o.getStatus() != OrderStatus.CANCELLED) {
                totalCommitted = totalCommitted.add(o.getTotalAmount());
            }
        }

        BigDecimal avgOrderValue = (deliveredCount > 0)
                ? totalDelivered.divide(BigDecimal.valueOf(deliveredCount), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        LocalDate start = LocalDate.now().minusDays(30);
        LocalDate end = LocalDate.now();
        List<PlannedItem> planned = plannedItemRepository.findByUserAndDateBetween(user.getId(), start, end);
        BigDecimal totalPlanned = planned.stream().map(PlannedItem::getLineTotal).reduce(BigDecimal.ZERO, BigDecimal::add);

        // Documented time saved formula: 4.5 minutes per planned item + 35 minutes per delivered order
        double timeMinutes = (planned.size() * 4.5) + (deliveredCount * 35.0);
        double timeSavedHours = Math.round((timeMinutes / 60.0) * 10.0) / 10.0;

        return AnalyticsSummaryDto.builder()
                .totalDeliveredSpend(totalDelivered.setScale(2, RoundingMode.HALF_UP))
                .totalCommittedSpend(totalCommitted.setScale(2, RoundingMode.HALF_UP))
                .totalPlannedSpend(totalPlanned.setScale(2, RoundingMode.HALF_UP))
                .totalDiscountSavings(totalDiscounts.setScale(2, RoundingMode.HALF_UP))
                .averageOrderValue(avgOrderValue)
                .totalOrdersPlaced(deliveredCount)
                .totalPlannedItems(planned.size())
                .timeSavedHours(timeSavedHours)
                .build();
    }

    @Transactional(readOnly = true)
    public List<SpendingDataPointDto> getWeeklySpendingChart(User user) {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(DayOfWeek.MONDAY);
        UserSettings settings = userSettingsRepository.findById(user.getId()).orElse(null);
        BigDecimal dailyBudget = (settings != null)
                ? settings.getWeeklyBudget().divide(BigDecimal.valueOf(7), 2, RoundingMode.HALF_UP)
                : new BigDecimal("700.00");

        List<SpendingDataPointDto> points = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate date = weekStart.plusDays(i);
            List<PlannedItem> items = plannedItemRepository.findByUserAndDateBetween(user.getId(), date, date);
            BigDecimal plannedAmt = items.stream().map(PlannedItem::getLineTotal).reduce(BigDecimal.ZERO, BigDecimal::add);

            Instant startDay = date.atStartOfDay().toInstant(ZoneOffset.UTC);
            Instant endDay = date.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);

            // Delivered orders on that day
            List<Order> orders = orderRepository.findByUserAndCreatedAtAfter(user, startDay).stream()
                    .filter(o -> o.getCreatedAt().isBefore(endDay) && o.getStatus() == OrderStatus.DELIVERED)
                    .toList();
            BigDecimal actualAmt = orders.stream().map(Order::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

            points.add(SpendingDataPointDto.builder()
                    .label(date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH))
                    .planned(plannedAmt.setScale(2, RoundingMode.HALF_UP))
                    .actual(actualAmt.setScale(2, RoundingMode.HALF_UP))
                    .budget(dailyBudget)
                    .build());
        }
        return points;
    }

    @Transactional(readOnly = true)
    public List<CategorySpendingDto> getCategoryBreakdown(User user) {
        Instant thirtyDaysAgo = Instant.now().minus(30, ChronoUnit.DAYS);
        List<Order> deliveredOrders = orderRepository.findByUserAndStatusAndCreatedAtAfter(user, OrderStatus.DELIVERED, thirtyDaysAgo);

        Map<String, BigDecimal> spendingByCategory = new HashMap<>();
        BigDecimal total = BigDecimal.ZERO;

        for (Order o : deliveredOrders) {
            for (OrderItem item : o.getItems()) {
                Category cat = item.getProduct().getCategory();
                String name = cat != null ? cat.getName() : "Other";
                spendingByCategory.put(name, spendingByCategory.getOrDefault(name, BigDecimal.ZERO).add(item.getLineTotal()));
                total = total.add(item.getLineTotal());
            }
        }

        // Also add planned items if orders are few
        if (total.compareTo(BigDecimal.ZERO) == 0) {
            LocalDate start = LocalDate.now().minusDays(14);
            LocalDate end = LocalDate.now().plusDays(7);
            List<PlannedItem> planned = plannedItemRepository.findByUserAndDateBetween(user.getId(), start, end);
            for (PlannedItem pi : planned) {
                Category cat = pi.getProduct().getCategory();
                String name = cat != null ? cat.getName() : "Other";
                spendingByCategory.put(name, spendingByCategory.getOrDefault(name, BigDecimal.ZERO).add(pi.getLineTotal()));
                total = total.add(pi.getLineTotal());
            }
        }

        List<CategorySpendingDto> result = new ArrayList<>();
        for (Map.Entry<String, BigDecimal> entry : spendingByCategory.entrySet()) {
            double pct = total.compareTo(BigDecimal.ZERO) > 0
                    ? entry.getValue().divide(total, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).doubleValue()
                    : 0.0;
            result.add(CategorySpendingDto.builder()
                    .categoryName(entry.getKey())
                    .amount(entry.getValue().setScale(2, RoundingMode.HALF_UP))
                    .percentage(pct)
                    .build());
        }

        result.sort((a, b) -> b.getAmount().compareTo(a.getAmount()));
        return result;
    }
}
