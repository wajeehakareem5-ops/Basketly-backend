package com.basketly.admin.controller;

import com.basketly.common.exception.ResourceNotFoundException;
import com.basketly.common.response.ApiResponse;
import com.basketly.delivery.domain.DeliverySlot;
import com.basketly.delivery.dto.DeliverySlotDto;
import com.basketly.delivery.repository.DeliverySlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/admin/delivery/slots")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminDeliveryController {

    private final DeliverySlotRepository deliverySlotRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<DeliverySlotDto>>> getAllSlots() {
        List<DeliverySlotDto> list = deliverySlotRepository.findAllByOrderBySlotDateAscStartTimeAsc()
                .stream().map(DeliverySlotDto::fromEntity).toList();
        return ResponseEntity.ok(ApiResponse.ok(list));
    }

    @PatchMapping("/{id}/capacity")
    @Transactional
    public ResponseEntity<ApiResponse<DeliverySlotDto>> updateCapacity(
            @PathVariable UUID id,
            @RequestBody Map<String, Integer> body) {
        int capacity = body.getOrDefault("capacity", 10);
        DeliverySlot slot = deliverySlotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Slot not found with id: " + id));

        slot.setCapacity(capacity);
        slot = deliverySlotRepository.save(slot);

        return ResponseEntity.ok(ApiResponse.ok("Slot capacity updated", DeliverySlotDto.fromEntity(slot)));
    }

    @PostMapping
    @Transactional
    public ResponseEntity<ApiResponse<DeliverySlotDto>> createSlot(@RequestBody Map<String, Object> body) {
        LocalDate slotDate = LocalDate.parse((String) body.get("slotDate"));
        LocalTime startTime = LocalTime.parse((String) body.get("startTime"));
        LocalTime endTime = LocalTime.parse((String) body.get("endTime"));
        int capacity = body.get("capacity") != null ? (Integer) body.get("capacity") : 10;

        DeliverySlot slot = DeliverySlot.builder()
                .slotDate(slotDate)
                .startTime(startTime)
                .endTime(endTime)
                .capacity(capacity)
                .bookedCount(0)
                .active(true)
                .build();

        slot = deliverySlotRepository.save(slot);
        return ResponseEntity.ok(ApiResponse.ok("Delivery slot created", DeliverySlotDto.fromEntity(slot)));
    }
}
