package com.basketly.planner.repository;

import com.basketly.planner.domain.PlannedItem;
import com.basketly.planner.domain.WeeklyPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlannedItemRepository extends JpaRepository<PlannedItem, UUID> {
    List<PlannedItem> findByWeeklyPlanOrderByPlanDateAscCreatedAtAsc(WeeklyPlan weeklyPlan);
    List<PlannedItem> findByWeeklyPlanAndPlanDateOrderByCreatedAtAsc(WeeklyPlan weeklyPlan, LocalDate planDate);
    Optional<PlannedItem> findByIdAndWeeklyPlan(UUID id, WeeklyPlan weeklyPlan);
    void deleteByWeeklyPlanAndPlanDate(WeeklyPlan weeklyPlan, LocalDate planDate);

    @Query("SELECT p FROM PlannedItem p WHERE p.weeklyPlan.user.id = :userId AND p.planDate BETWEEN :startDate AND :endDate")
    List<PlannedItem> findByUserAndDateBetween(
            @Param("userId") UUID userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
