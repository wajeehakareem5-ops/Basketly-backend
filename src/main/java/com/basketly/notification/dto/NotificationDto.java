package com.basketly.notification.dto;

import com.basketly.notification.domain.Notification;
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
public class NotificationDto {
    private UUID id;
    private String title;
    private String message;
    private String type;
    private String deepLink;
    private boolean read;
    private Instant createdAt;

    public static NotificationDto fromEntity(Notification n) {
        return NotificationDto.builder()
                .id(n.getId())
                .title(n.getTitle())
                .message(n.getMessage())
                .type(n.getType())
                .deepLink(n.getDeepLink())
                .read(n.isRead())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
