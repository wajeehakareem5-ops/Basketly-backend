package com.basketly.delivery;

import com.basketly.common.exception.BadRequestException;
import com.basketly.delivery.domain.DeliveryReservation;
import com.basketly.delivery.domain.DeliverySlot;
import com.basketly.delivery.dto.ReservationDto;
import com.basketly.delivery.dto.ReservationRequest;
import com.basketly.delivery.repository.DeliveryReservationRepository;
import com.basketly.delivery.repository.DeliverySlotRepository;
import com.basketly.delivery.service.DeliveryService;
import com.basketly.user.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeliveryServiceTest {

    @Mock
    private DeliverySlotRepository deliverySlotRepository;

    @Mock
    private DeliveryReservationRepository deliveryReservationRepository;

    @InjectMocks
    private DeliveryService deliveryService;

    @Test
    void createReservation_SuccessWhenCapacityAvailable() {
        User user = User.builder().id(UUID.randomUUID()).build();
        UUID slotId = UUID.randomUUID();

        DeliverySlot slot = DeliverySlot.builder()
                .id(slotId)
                .slotDate(LocalDate.now().plusDays(1))
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(11, 0))
                .capacity(10)
                .bookedCount(5)
                .active(true)
                .build();

        ReservationRequest request = ReservationRequest.builder()
                .slotId(slotId)
                .notes("Ring the doorbell")
                .build();

        when(deliverySlotRepository.findById(slotId)).thenReturn(Optional.of(slot));
        when(deliverySlotRepository.incrementBookedCountIfAvailable(slotId)).thenReturn(1);
        when(deliveryReservationRepository.save(any(DeliveryReservation.class))).thenAnswer(invocation -> {
            DeliveryReservation r = invocation.getArgument(0);
            r.setId(UUID.randomUUID());
            return r;
        });

        ReservationDto dto = deliveryService.createReservation(user, request);

        assertNotNull(dto);
        assertEquals("RESERVED", dto.getStatus());
        assertEquals("Ring the doorbell", dto.getNotes());
        verify(deliverySlotRepository).incrementBookedCountIfAvailable(slotId);
    }

    @Test
    void createReservation_ThrowsWhenCapacityExhausted() {
        User user = User.builder().id(UUID.randomUUID()).build();
        UUID slotId = UUID.randomUUID();

        DeliverySlot slot = DeliverySlot.builder()
                .id(slotId)
                .capacity(10)
                .bookedCount(10)
                .active(true)
                .build();

        ReservationRequest request = ReservationRequest.builder()
                .slotId(slotId)
                .build();

        when(deliverySlotRepository.findById(slotId)).thenReturn(Optional.of(slot));
        // Atomic update returns 0 rows affected
        when(deliverySlotRepository.incrementBookedCountIfAvailable(slotId)).thenReturn(0);

        assertThrows(BadRequestException.class, () -> deliveryService.createReservation(user, request));
    }
}
