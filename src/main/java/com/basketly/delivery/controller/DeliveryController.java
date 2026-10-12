package com.basketly.delivery.controller;

import com.basketly.common.response.ApiResponse;
import com.basketly.delivery.dto.DeliverySlotDto;
import com.basketly.delivery.dto.ReservationDto;
import com.basketly.delivery.dto.ReservationRequest;
import com.basketly.delivery.service.DeliveryService;
import com.basketly.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/delivery")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryService deliveryService;

    @GetMapping("/slots")
    public ResponseEntity<ApiResponse<List<DeliverySlotDto>>> getSlots(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<DeliverySlotDto> slots = deliveryService.getSlots(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.ok(slots));
    }

    @GetMapping("/schedule")
    public ResponseEntity<ApiResponse<List<ReservationDto>>> getSchedule(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<ReservationDto> schedule = deliveryService.getUserSchedule(userDetails.getUser());
        return ResponseEntity.ok(ApiResponse.ok(schedule));
    }

    @PostMapping("/slots/reservations")
    public ResponseEntity<ApiResponse<ReservationDto>> createReservation(
            @Valid @RequestBody ReservationRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        ReservationDto reservation = deliveryService.createReservation(userDetails.getUser(), request);
        return ResponseEntity.ok(ApiResponse.ok("Delivery slot reserved successfully", reservation));
    }

    @PatchMapping("/reservations/{id}")
    public ResponseEntity<ApiResponse<ReservationDto>> rescheduleReservation(
            @PathVariable UUID id,
            @RequestBody Map<String, UUID> body,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        UUID newSlotId = body.get("slotId");
        ReservationDto reservation = deliveryService.rescheduleReservation(userDetails.getUser(), id, newSlotId);
        return ResponseEntity.ok(ApiResponse.ok("Delivery slot rescheduled", reservation));
    }

    @DeleteMapping("/reservations/{id}")
    public ResponseEntity<ApiResponse<Void>> cancelReservation(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        deliveryService.cancelReservation(userDetails.getUser(), id);
        return ResponseEntity.ok(ApiResponse.ok("Reservation cancelled successfully", null));
    }
}
