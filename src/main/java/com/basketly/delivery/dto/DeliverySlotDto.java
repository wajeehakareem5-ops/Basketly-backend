package com.basketly.delivery.dto;

import com.basketly.delivery.domain.DeliverySlot;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliverySlotDto {
    private UUID id;
    private LocalDate slotDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private int capacity;
    private int bookedCount;
    private int availableCount;
    private boolean available;

    public static DeliverySlotDto fromEntity(DeliverySlot slot) {
        return DeliverySlotDto.builder()
                .id(slot.getId())
                .slotDate(slot.getSlotDate())
                .startTime(slot.getStartTime())
                .endTime(slot.getEndTime())
                .capacity(slot.getCapacity())
                .bookedCount(slot.getBookedCount())
                .availableCount(slot.getAvailableCapacity())
                .available(slot.isAvailable())
                .build();
    }
}
