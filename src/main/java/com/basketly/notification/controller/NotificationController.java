package com.basketly.notification.controller;

import com.basketly.common.response.ApiResponse;
import com.basketly.notification.dto.NotificationDto;
import com.basketly.notification.service.NotificationService;
import com.basketly.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getNotifications(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<NotificationDto> notifications = notificationService.getNotifications(userDetails.getUser());
        long unreadCount = notificationService.getUnreadCount(userDetails.getUser());

        Map<String, Object> data = new HashMap<>();
        data.put("notifications", notifications);
        data.put("unreadCount", unreadCount);

        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markRead(
            @PathVariable UUID id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        notificationService.markRead(userDetails.getUser(), id);
        return ResponseEntity.ok(ApiResponse.ok("Notification marked as read", null));
    }

    @PostMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllRead(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        notificationService.markAllRead(userDetails.getUser());
        return ResponseEntity.ok(ApiResponse.ok("All notifications marked as read", null));
    }
}
