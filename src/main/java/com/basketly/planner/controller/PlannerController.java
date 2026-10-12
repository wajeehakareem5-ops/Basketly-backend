package com.basketly.planner.controller;

import com.basketly.common.response.ApiResponse;
import com.basketly.planner.dto.*;
import com.basketly.planner.service.PlannerService;
import com.basketly.recurrence.dto.CreateRecurringRuleRequest;
import com.basketly.recurrence.dto.RecurrencePreviewDto;
import com.basketly.recurrence.service.RecurrenceService;
import com.basketly.security.CustomUserDetails;
import com.basketly.user.domain.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/planner")
@RequiredArgsConstructor
public class PlannerController {

    private final PlannerService plannerService;
    private final RecurrenceService recurrenceService;

    private User getUser(CustomUserDetails userDetails) {
        return userDetails.getUser();
    }

    @GetMapping("/weeks/{weekStart}")
    public ResponseEntity<ApiResponse<WeeklyPlanDto>> getWeeklyPlan(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        WeeklyPlanDto plan = plannerService.getWeeklyPlanDto(getUser(userDetails), weekStart);
        return ResponseEntity.ok(ApiResponse.ok(plan));
    }

    @PostMapping("/weeks")
    public ResponseEntity<ApiResponse<WeeklyPlanDto>> createOrGetWeek(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        WeeklyPlanDto plan = plannerService.getWeeklyPlanDto(getUser(userDetails), weekStart);
        return ResponseEntity.ok(ApiResponse.ok("Weekly plan loaded", plan));
    }

    @PostMapping("/weeks/copy")
    public ResponseEntity<ApiResponse<Void>> copyWeek(
            @Valid @RequestBody CopyWeekRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        plannerService.copyWeek(getUser(userDetails), request.getSourceWeekStart(), request.getTargetWeekStart());
        return ResponseEntity.ok(ApiResponse.ok("Week copied successfully", null));
    }

    @PostMapping("/days/{date}/items")
    public ResponseEntity<ApiResponse<PlannedItemDto>> addPlannedItem(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Valid @RequestBody CreatePlannedItemRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        request.setPlanDate(date);
        PlannedItemDto item = plannerService.addPlannedItem(getUser(userDetails), request);
        return ResponseEntity.ok(ApiResponse.ok("Item added to plan", item));
    }

    @PostMapping("/days/{date}/copy")
    public ResponseEntity<ApiResponse<Void>> copyDay(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Valid @RequestBody CopyDayRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        plannerService.copyDay(getUser(userDetails), date, request.getTargetDate());
        return ResponseEntity.ok(ApiResponse.ok("Day items copied successfully", null));
    }

    @DeleteMapping("/days/{date}")
    public ResponseEntity<ApiResponse<Void>> clearDay(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        plannerService.clearDay(getUser(userDetails), date);
        return ResponseEntity.ok(ApiResponse.ok("Day cleared successfully", null));
    }

    @PatchMapping("/items/{id}")
    public ResponseEntity<ApiResponse<PlannedItemDto>> updatePlannedItem(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePlannedItemRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        PlannedItemDto item = plannerService.updatePlannedItem(getUser(userDetails), id, request);
        return ResponseEntity.ok(ApiResponse.ok("Item updated", item));
    }

    @DeleteMapping("/items/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePlannedItem(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        plannerService.deletePlannedItem(getUser(userDetails), id);
        return ResponseEntity.ok(ApiResponse.ok("Item removed from plan", null));
    }

    @PostMapping("/items/{id}/complete")
    public ResponseEntity<ApiResponse<PlannedItemDto>> toggleComplete(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        PlannedItemDto item = plannerService.toggleCompleted(getUser(userDetails), id);
        return ResponseEntity.ok(ApiResponse.ok(item.isCompleted() ? "Item marked completed" : "Item marked pending", item));
    }

    @PostMapping("/recurrences/preview")
    public ResponseEntity<ApiResponse<RecurrencePreviewDto>> previewRecurrence(
            @Valid @RequestBody CreateRecurringRuleRequest request) {
        RecurrencePreviewDto preview = recurrenceService.previewRecurrence(request);
        return ResponseEntity.ok(ApiResponse.ok(preview));
    }

    @PostMapping("/recurrences")
    public ResponseEntity<ApiResponse<Void>> createRecurringRule(
            @Valid @RequestBody CreateRecurringRuleRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        recurrenceService.createRecurringRule(getUser(userDetails), request);
        return ResponseEntity.ok(ApiResponse.ok("Recurring rule created successfully", null));
    }
}
