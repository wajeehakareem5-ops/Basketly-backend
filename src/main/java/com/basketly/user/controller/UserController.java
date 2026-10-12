package com.basketly.user.controller;

import com.basketly.common.response.ApiResponse;
import com.basketly.security.CustomUserDetails;
import com.basketly.user.dto.UserSettingsDto;
import com.basketly.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/settings")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<UserSettingsDto>> getSettings(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        UserSettingsDto settings = userService.getSettings(userDetails.getUser());
        return ResponseEntity.ok(ApiResponse.ok(settings));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<UserSettingsDto>> updateSettings(
            @Valid @RequestBody UserSettingsDto dto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        UserSettingsDto updated = userService.updateSettings(userDetails.getUser(), dto);
        return ResponseEntity.ok(ApiResponse.ok("Settings saved successfully", updated));
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");
        userService.changePassword(userDetails.getUser(), oldPassword, newPassword);
        return ResponseEntity.ok(ApiResponse.ok("Password changed successfully", null));
    }
}
