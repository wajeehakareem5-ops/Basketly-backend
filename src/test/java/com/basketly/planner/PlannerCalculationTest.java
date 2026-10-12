package com.basketly.planner;

import com.basketly.catalog.domain.Category;
import com.basketly.catalog.domain.Product;
import com.basketly.planner.domain.PlannedItem;
import com.basketly.planner.domain.WeeklyPlan;
import com.basketly.planner.dto.WeeklyPlanDto;
import com.basketly.planner.repository.PlannedItemRepository;
import com.basketly.planner.repository.WeeklyPlanRepository;
import com.basketly.planner.service.PlannerService;
import com.basketly.recurrence.service.RecurrenceService;
import com.basketly.user.domain.User;
import com.basketly.user.domain.UserSettings;
import com.basketly.user.repository.UserSettingsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlannerCalculationTest {

    @Mock
    private WeeklyPlanRepository weeklyPlanRepository;

    @Mock
    private PlannedItemRepository plannedItemRepository;

    @Mock
    private UserSettingsRepository userSettingsRepository;

    @Mock
    private RecurrenceService recurrenceService;

    @InjectMocks
    private PlannerService plannerService;

    @Test
    void testLineTotalCalculation() {
        Product milk = Product.builder()
                .id(UUID.randomUUID())
                .name("Milk")
                .price(new BigDecimal("32.50"))
                .build();

        PlannedItem item = PlannedItem.builder()
                .product(milk)
                .unitPrice(milk.getPrice())
                .quantity(3)
                .build();

        assertEquals(new BigDecimal("97.50"), item.getLineTotal());
    }

    @Test
    void testWeeklyTotalsAndBudgetPercentage() {
        User user = User.builder().id(UUID.randomUUID()).email("user@basketly.app").build();
        LocalDate monday = LocalDate.of(2026, 10, 12);

        WeeklyPlan plan = WeeklyPlan.builder()
                .id(UUID.randomUUID())
                .user(user)
                .weekStartDate(monday)
                .build();

        Product bread = Product.builder().id(UUID.randomUUID()).name("Bread").price(new BigDecimal("50.00")).build();
        Product eggs = Product.builder().id(UUID.randomUUID()).name("Eggs").price(new BigDecimal("96.00")).build();

        PlannedItem item1 = PlannedItem.builder()
                .id(UUID.randomUUID())
                .weeklyPlan(plan)
                .product(bread)
                .planDate(monday)
                .quantity(2)
                .unitPrice(new BigDecimal("50.00"))
                .build();

        PlannedItem item2 = PlannedItem.builder()
                .id(UUID.randomUUID())
                .weeklyPlan(plan)
                .product(eggs)
                .planDate(monday.plusDays(2))
                .quantity(1)
                .unitPrice(new BigDecimal("96.00"))
                .build();

        when(weeklyPlanRepository.findByUserAndWeekStartDate(user, monday)).thenReturn(Optional.of(plan));
        when(plannedItemRepository.findByWeeklyPlanOrderByPlanDateAscCreatedAtAsc(plan)).thenReturn(List.of(item1, item2));

        UserSettings settings = UserSettings.builder()
                .userId(user.getId())
                .weeklyBudget(new BigDecimal("1000.00"))
                .build();
        when(userSettingsRepository.findById(user.getId())).thenReturn(Optional.of(settings));

        WeeklyPlanDto result = plannerService.getWeeklyPlanDto(user, monday);

        assertNotNull(result);
        // item1 line total = 100.00, item2 line total = 96.00 -> weekly total = 196.00
        assertEquals(new BigDecimal("196.00"), result.getWeeklyTotalAmount());
        assertEquals(2, result.getTotalItemCount());
        // Remaining = 1000.00 - 196.00 = 804.00
        assertEquals(new BigDecimal("804.00"), result.getRemainingBudget());
        // Percentage used = (196.00 / 1000.00) * 100 = 19.6%
        assertEquals(19.6, result.getBudgetPercentageUsed(), 0.01);
    }
}
