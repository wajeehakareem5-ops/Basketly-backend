package com.basketly.user.dto;

import com.basketly.user.domain.UserSettings;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSettingsDto {

    @NotNull(message = "Weekly budget is required")
    @DecimalMin(value = "0.00", message = "Weekly budget must be non-negative")
    private BigDecimal weeklyBudget;

    @NotNull(message = "Monthly budget is required")
    @DecimalMin(value = "0.00", message = "Monthly budget must be non-negative")
    private BigDecimal monthlyBudget;

    private String weekStartDay;
    private String timezone;
    private boolean emailNotifications;
    private boolean budgetAlerts;

    public static UserSettingsDto fromEntity(UserSettings settings) {
        return UserSettingsDto.builder()
                .weeklyBudget(settings.getWeeklyBudget())
                .monthlyBudget(settings.getMonthlyBudget())
                .weekStartDay(settings.getWeekStartDay())
                .timezone(settings.getTimezone())
                .emailNotifications(settings.isEmailNotifications())
                .budgetAlerts(settings.isBudgetAlerts())
                .build();
    }
}
