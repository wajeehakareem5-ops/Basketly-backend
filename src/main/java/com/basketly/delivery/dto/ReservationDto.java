package com.basketly.delivery.dto;

import com.basketly.delivery.domain.DeliveryReservation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationDto {
    private UUID id;
    private DeliverySlotDto slot;
    private String status;
    private String notes;
    private Instant createdAt;

    public static ReservationDto fromEntity(DeliveryReservation reservation) {
        return ReservationDto.builder()
                .id(reservation.getId())
                .slot(DeliverySlotDto.fromEntity(reservation.getSlot()))
                .status(reservation.getStatus())
                .notes(reservation.getNotes())
                .createdAt(reservation.getCreatedAt())
                .build();
    }
}
