package com.basketly.user.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSettings {

    @Id
    @Column(name = "user_id", columnDefinition = "BINARY(16)")
    private UUID userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "weekly_budget", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal weeklyBudget = new BigDecimal("5000.00");

    @Column(name = "monthly_budget", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal monthlyBudget = new BigDecimal("20000.00");

    @Column(name = "week_start_day", nullable = false, length = 10)
    @Builder.Default
    private String weekStartDay = "MONDAY";

    @Column(name = "timezone", nullable = false, length = 50)
    @Builder.Default
    private String timezone = "Asia/Kolkata";

    @Column(name = "email_notifications", nullable = false)
    @Builder.Default
    private boolean emailNotifications = true;

    @Column(name = "budget_alerts", nullable = false)
    @Builder.Default
    private boolean budgetAlerts = true;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
        if (this.updatedAt == null) {
            this.updatedAt = Instant.now();
        }
        if (this.weeklyBudget == null) {
            this.weeklyBudget = new BigDecimal("5000.00");
        }
        if (this.monthlyBudget == null) {
            this.monthlyBudget = new BigDecimal("20000.00");
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }
}
