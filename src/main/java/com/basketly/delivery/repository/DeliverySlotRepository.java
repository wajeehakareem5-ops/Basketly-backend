package com.basketly.delivery.repository;

import com.basketly.delivery.domain.DeliverySlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface DeliverySlotRepository extends JpaRepository<DeliverySlot, UUID> {

    List<DeliverySlot> findBySlotDateBetweenAndActiveTrueOrderBySlotDateAscStartTimeAsc(LocalDate startDate, LocalDate endDate);

    List<DeliverySlot> findBySlotDateAndActiveTrueOrderByStartTimeAsc(LocalDate slotDate);

    List<DeliverySlot> findAllByOrderBySlotDateAscStartTimeAsc();

    @Modifying
    @Query("UPDATE DeliverySlot s SET s.bookedCount = s.bookedCount + 1 WHERE s.id = :id AND s.bookedCount < s.capacity")
    int incrementBookedCountIfAvailable(@Param("id") UUID id);

    @Modifying
    @Query("UPDATE DeliverySlot s SET s.bookedCount = CASE WHEN s.bookedCount > 0 THEN s.bookedCount - 1 ELSE 0 END WHERE s.id = :id")
    int decrementBookedCount(@Param("id") UUID id);
}
