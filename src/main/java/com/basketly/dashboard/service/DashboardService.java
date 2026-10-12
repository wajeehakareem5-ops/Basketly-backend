package com.basketly.dashboard.service;

import com.basketly.dashboard.dto.DashboardSummaryDto;
import com.basketly.order.domain.Order;
import com.basketly.order.dto.OrderDto;
import com.basketly.order.repository.OrderRepository;
import com.basketly.planner.dto.WeeklyPlanDto;
import com.basketly.planner.service.PlannerService;
import com.basketly.suggestion.dto.SuggestionDto;
import com.basketly.suggestion.service.SuggestionService;
import com.basketly.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final PlannerService plannerService;
    private final OrderRepository orderRepository;
    private final SuggestionService suggestionService;

    @Transactional(readOnly = true)
    public DashboardSummaryDto getDashboardSummary(User user) {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(DayOfWeek.MONDAY);
        LocalDate weekEnd = weekStart.plusDays(6);

        // Weekly Plan details
        WeeklyPlanDto weeklyPlan = plannerService.getWeeklyPlanDto(user, weekStart);

        // Upcoming Deliveries
        List<Order> deliveries = orderRepository.findUpcomingDeliveries(user, today);
        List<OrderDto> deliveryDtos = deliveries.stream().map(OrderDto::fromEntity).toList();

        // Smart Suggestions
        List<SuggestionDto> suggestions = suggestionService.generateSuggestions(user);

        // Time Saved Calculation
        // Formula: 4.5 minutes per planned item + 35 minutes per delivery order
        int totalItems = weeklyPlan.getTotalItemCount();
        int totalDeliveries = deliveries.size();
        double totalMinutesSaved = (totalItems * 4.5) + (totalDeliveries * 35.0);
        long hours = (long) (totalMinutesSaved / 60);
        long mins = (long) (totalMinutesSaved % 60);
        String timeDisplay = hours > 0 ? String.format("%d hr %d min", hours, mins) : String.format("%d min", mins);
        String formula = "Formula: (Planned Items × 4.5m) + (Confirmed Deliveries × 35m) estimated vs in-store supermarket trips";

        // Dynamic greeting
        int hour = LocalTime.now().getHour();
        String greetingTime = (hour < 12) ? "Good morning" : (hour < 17) ? "Good afternoon" : "Good evening";
        String firstName = user.getFullName() != null ? user.getFullName().split(" ")[0] : "there";
        String greeting = greetingTime + ", " + firstName + "!";

        return DashboardSummaryDto.builder()
                .greeting(greeting)
                .userName(user.getFullName())
                .weekStartDate(weekStart)
                .weekEndDate(weekEnd)
                .estimatedWeeklySpending(weeklyPlan.getWeeklyTotalAmount())
                .plannedItemCount(totalItems)
                .scheduledOrdersCount(totalDeliveries)
                .confirmedOrdersCount(totalDeliveries)
                .weeklyBudget(weeklyPlan.getWeeklyBudget())
                .remainingBudget(weeklyPlan.getRemainingBudget())
                .budgetPercentageUsed(weeklyPlan.getBudgetPercentageUsed())
                .timeSavedDisplay(timeDisplay)
                .timeSavedFormula(formula)
                .weeklyPlan(weeklyPlan)
                .upcomingDeliveries(deliveryDtos)
                .suggestions(suggestions)
                .build();
    }
}
