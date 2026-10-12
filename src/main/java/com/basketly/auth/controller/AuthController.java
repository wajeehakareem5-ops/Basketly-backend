package com.basketly.auth.controller;

import com.basketly.auth.dto.*;
import com.basketly.auth.service.AuthService;
import com.basketly.common.response.ApiResponse;
import com.basketly.security.CustomUserDetails;
import com.basketly.user.dto.UserResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final String REFRESH_COOKIE_NAME = "basketly_refresh_token";
    private final AuthService authService;

    private void setRefreshCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_COOKIE_NAME, refreshToken)
                .httpOnly(true)
                .secure(false) // Set to true in prod with HTTPS
                .sameSite("Lax")
                .path("/api/v1/auth")
                .maxAge(7 * 24 * 60 * 60)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void clearRefreshCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/api/v1/auth")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private String extractRefreshCookie(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }
        return Arrays.stream(request.getCookies())
                .filter(c -> REFRESH_COOKIE_NAME.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletResponse response) {
        AuthService.AuthResult result = authService.register(request);
        setRefreshCookie(response, result.rawRefreshToken());
        AuthResponse authResponse = AuthResponse.builder()
                .accessToken(result.accessToken())
                .user(result.user())
                .build();
        return ResponseEntity.ok(ApiResponse.ok("Registration successful", authResponse));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response) {
        AuthService.AuthResult result = authService.login(request);
        setRefreshCookie(response, result.rawRefreshToken());
        AuthResponse authResponse = AuthResponse.builder()
                .accessToken(result.accessToken())
                .user(result.user())
                .build();
        return ResponseEntity.ok(ApiResponse.ok("Login successful", authResponse));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            HttpServletRequest request,
            HttpServletResponse response) {
        String rawRefreshToken = extractRefreshCookie(request);
        AuthService.AuthResult result = authService.refresh(rawRefreshToken);
        setRefreshCookie(response, result.rawRefreshToken());
        AuthResponse authResponse = AuthResponse.builder()
                .accessToken(result.accessToken())
                .user(result.user())
                .build();
        return ResponseEntity.ok(ApiResponse.ok("Token refreshed successfully", authResponse));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            HttpServletRequest request,
            HttpServletResponse response) {
        String rawRefreshToken = extractRefreshCookie(request);
        authService.logout(rawRefreshToken);
        clearRefreshCookie(response);
        return ResponseEntity.ok(ApiResponse.ok("Logged out successfully", null));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Map<String, String>>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        String token = authService.forgotPassword(request);
        // For development convenience, return token in response if email server is not configured
        return ResponseEntity.ok(ApiResponse.ok("Password reset instructions generated", Map.of(
                "token", token,
                "message", "If an account exists, password reset token has been created."
        )));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.ok("Password reset successfully. You may now log in.", null));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getMe(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        UserResponse response = UserResponse.fromEntity(userDetails.getUser());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
