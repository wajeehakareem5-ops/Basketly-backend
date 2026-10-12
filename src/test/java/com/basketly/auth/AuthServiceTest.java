package com.basketly.auth;

import com.basketly.auth.domain.RefreshToken;
import com.basketly.auth.dto.LoginRequest;
import com.basketly.auth.dto.RegisterRequest;
import com.basketly.auth.repository.PasswordResetTokenRepository;
import com.basketly.auth.repository.RefreshTokenRepository;
import com.basketly.auth.service.AuthService;
import com.basketly.common.exception.BadRequestException;
import com.basketly.common.exception.ConflictException;
import com.basketly.security.JwtService;
import com.basketly.user.domain.Role;
import com.basketly.user.domain.User;
import com.basketly.user.repository.UserRepository;
import com.basketly.user.repository.UserSettingsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserSettingsRepository userSettingsRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "refreshTokenExpirationMs", 604800000L);
    }

    @Test
    void register_Success() {
        RegisterRequest request = RegisterRequest.builder()
                .fullName("Test User")
                .email("test@basketly.app")
                .password("Password@123")
                .build();

        when(userRepository.existsByEmail("test@basketly.app")).thenReturn(false);
        when(passwordEncoder.encode("Password@123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(UUID.randomUUID());
            return u;
        });
        when(jwtService.generateAccessToken(any(User.class))).thenReturn("access_token_123");

        AuthService.AuthResult result = authService.register(request);

        assertNotNull(result);
        assertEquals("access_token_123", result.accessToken());
        assertEquals("test@basketly.app", result.user().getEmail());
        verify(userSettingsRepository, times(1)).save(any());
    }

    @Test
    void register_DuplicateEmail_ThrowsConflictException() {
        RegisterRequest request = RegisterRequest.builder()
                .fullName("Test User")
                .email("duplicate@basketly.app")
                .password("Password@123")
                .build();

        when(userRepository.existsByEmail("duplicate@basketly.app")).thenReturn(true);

        assertThrows(ConflictException.class, () -> authService.register(request));
    }

    @Test
    void login_Success() {
        LoginRequest request = LoginRequest.builder()
                .email("user@basketly.app")
                .password("Password@123")
                .build();

        User user = User.builder()
                .id(UUID.randomUUID())
                .email("user@basketly.app")
                .fullName("Test User")
                .role(Role.USER)
                .active(true)
                .build();

        when(userRepository.findByEmail("user@basketly.app")).thenReturn(Optional.of(user));
        when(jwtService.generateAccessToken(user)).thenReturn("valid_jwt_token");

        AuthService.AuthResult result = authService.login(request);

        assertNotNull(result);
        assertEquals("valid_jwt_token", result.accessToken());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void refresh_RevokedToken_ThrowsBadRequestException() {
        RefreshToken token = RefreshToken.builder()
                .id(UUID.randomUUID())
                .tokenHash("somehash")
                .revoked(true)
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(token));

        assertThrows(BadRequestException.class, () -> authService.refresh("raw_token"));
    }
}
