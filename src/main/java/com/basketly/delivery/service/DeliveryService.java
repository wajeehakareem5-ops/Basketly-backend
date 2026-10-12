package com.basketly.delivery.service;

import com.basketly.common.exception.BadRequestException;
import com.basketly.common.exception.ResourceNotFoundException;
import com.basketly.delivery.domain.DeliveryReservation;
import com.basketly.delivery.domain.DeliverySlot;
import com.basketly.delivery.dto.DeliverySlotDto;
import com.basketly.delivery.dto.ReservationDto;
import com.basketly.delivery.dto.ReservationRequest;
import com.basketly.delivery.repository.DeliveryReservationRepository;
import com.basketly.delivery.repository.DeliverySlotRepository;
import com.basketly.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeliveryService {

    private final DeliverySlotRepository deliverySlotRepository;
    private final DeliveryReservationRepository deliveryReservationRepository;

    @Transactional(readOnly = true)
    public List<DeliverySlotDto> getSlots(LocalDate startDate, LocalDate endDate) {
        if (startDate == null) {
            startDate = LocalDate.now();
        }
        if (endDate == null) {
            endDate = startDate.plusDays(14);
        }

        return deliverySlotRepository.findBySlotDateBetweenAndActiveTrueOrderBySlotDateAscStartTimeAsc(startDate, endDate).stream()
                .map(DeliverySlotDto::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReservationDto> getUserSchedule(User user) {
        return deliveryReservationRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(ReservationDto::fromEntity)
                .toList();
    }

    @Transactional
    public ReservationDto createReservation(User user, ReservationRequest request) {
        DeliverySlot slot = deliverySlotRepository.findById(request.getSlotId())
                .orElseThrow(() -> new ResourceNotFoundException("Delivery slot not found with id: " + request.getSlotId()));

        // Atomic TiDB-safe capacity increment
        int updatedRows = deliverySlotRepository.incrementBookedCountIfAvailable(slot.getId());
        if (updatedRows == 0) {
            throw new BadRequestException("Selected delivery slot is fully booked. Please choose another time slot.");
        }

        DeliveryReservation reservation = DeliveryReservation.builder()
                .user(user)
                .slot(slot)
                .status("RESERVED")
                .notes(request.getNotes())
                .build();

        reservation = deliveryReservationRepository.save(reservation);
        return ReservationDto.fromEntity(reservation);
    }

    @Transactional
    public DeliveryReservation reserveSlotEntity(User user, UUID slotId, String notes) {
        DeliverySlot slot = deliverySlotRepository.findById(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery slot not found with id: " + slotId));

        int updatedRows = deliverySlotRepository.incrementBookedCountIfAvailable(slot.getId());
        if (updatedRows == 0) {
            throw new BadRequestException("Selected delivery slot is fully booked. Please choose another time slot.");
        }

        DeliveryReservation reservation = DeliveryReservation.builder()
                .user(user)
                .slot(slot)
                .status("RESERVED")
                .notes(notes)
                .build();

        return deliveryReservationRepository.save(reservation);
    }

    @Transactional
    public ReservationDto rescheduleReservation(User user, UUID reservationId, UUID newSlotId) {
        DeliveryReservation reservation = deliveryReservationRepository.findByIdAndUser(reservationId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with id: " + reservationId));

        if ("CANCELLED".equals(reservation.getStatus())) {
            throw new BadRequestException("Cannot reschedule a cancelled reservation");
        }

        DeliverySlot oldSlot = reservation.getSlot();
        if (oldSlot.getId().equals(newSlotId)) {
            return ReservationDto.fromEntity(reservation);
        }

        DeliverySlot newSlot = deliverySlotRepository.findById(newSlotId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery slot not found with id: " + newSlotId));

        // Atomic increment on new slot
        int updated = deliverySlotRepository.incrementBookedCountIfAvailable(newSlot.getId());
        if (updated == 0) {
            throw new BadRequestException("New delivery slot is fully booked. Please select another slot.");
        }

        // Atomic release on old slot
        deliverySlotRepository.decrementBookedCount(oldSlot.getId());

        reservation.setSlot(newSlot);
        reservation = deliveryReservationRepository.save(reservation);
        return ReservationDto.fromEntity(reservation);
    }

    @Transactional
    public void cancelReservation(User user, UUID reservationId) {
        DeliveryReservation reservation = deliveryReservationRepository.findByIdAndUser(reservationId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with id: " + reservationId));

        if (!"CANCELLED".equals(reservation.getStatus())) {
            deliverySlotRepository.decrementBookedCount(reservation.getSlot().getId());
            reservation.setStatus("CANCELLED");
            deliveryReservationRepository.save(reservation);
        }
    }
}
