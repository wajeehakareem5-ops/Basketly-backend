package com.basketly.planner.repository;

import com.basketly.planner.domain.WeeklyPlan;
import com.basketly.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WeeklyPlanRepository extends JpaRepository<WeeklyPlan, UUID> {
    Optional<WeeklyPlan> findByUserAndWeekStartDate(User user, LocalDate weekStartDate);
}
