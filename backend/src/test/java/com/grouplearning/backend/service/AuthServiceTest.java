package com.grouplearning.backend.service;

import com.grouplearning.backend.dto.request.LoginRequest;
import com.grouplearning.backend.dto.request.RegisterRequest;
import com.grouplearning.backend.dto.response.UserResponse;
import com.grouplearning.backend.entity.User;
import com.grouplearning.backend.exception.ConflictException;
import com.grouplearning.backend.exception.UnauthorizedException;
import com.grouplearning.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

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
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_shouldCreateUser_whenRequestIsValid() {
        RegisterRequest request = new RegisterRequest(
                "hieu",
                "hieu@example.com",
                "12345678"
        );

        when(userRepository.existsByUsernameIgnoreCase("hieu"))
                .thenReturn(false);

        when(userRepository.existsByEmailIgnoreCase("hieu@example.com"))
                .thenReturn(false);

        when(passwordEncoder.encode("12345678"))
                .thenReturn("hashed-password");

        User savedUser = mock(User.class);

        UUID userId = UUID.randomUUID();
        Instant createdAt = Instant.now();

        when(savedUser.getId()).thenReturn(userId);
        when(savedUser.getUsername()).thenReturn("hieu");
        when(savedUser.getEmail()).thenReturn("hieu@example.com");
        when(savedUser.getCreatedAt()).thenReturn(createdAt);

        when(userRepository.save(any(User.class)))
                .thenReturn(savedUser);

        UserResponse response = authService.register(request);

        assertEquals(userId, response.id());
        assertEquals("hieu", response.username());
        assertEquals("hieu@example.com", response.email());

        verify(passwordEncoder).encode("12345678");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_shouldThrowConflict_whenEmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest(
                "hieu",
                "hieu@example.com",
                "12345678"
        );

        when(userRepository.existsByUsernameIgnoreCase("hieu"))
                .thenReturn(false);

        when(userRepository.existsByEmailIgnoreCase("hieu@example.com"))
                .thenReturn(true);

        assertThrows(
                ConflictException.class,
                () -> authService.register(request)
        );

        verify(userRepository, never()).save(any());
    }

    @Test
    void login_shouldReturnUser_whenCredentialsAreValid() {
        LoginRequest request = new LoginRequest(
                "hieu@example.com",
                "12345678"
        );

        User user = mock(User.class);

        UUID userId = UUID.randomUUID();
        Instant createdAt = Instant.now();

        when(user.getId()).thenReturn(userId);
        when(user.getUsername()).thenReturn("hieu");
        when(user.getEmail()).thenReturn("hieu@example.com");
        when(user.getPasswordHash()).thenReturn("hashed-password");
        when(user.getCreatedAt()).thenReturn(createdAt);

        when(userRepository.findByEmailIgnoreCase("hieu@example.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "12345678",
                "hashed-password"
        )).thenReturn(true);

        UserResponse response = authService.login(request);

        assertEquals(userId, response.id());
        assertEquals("hieu", response.username());
        assertEquals("hieu@example.com", response.email());
    }

    @Test
    void login_shouldThrowUnauthorized_whenPasswordIsInvalid() {
        LoginRequest request = new LoginRequest(
                "hieu@example.com",
                "wrong-password"
        );

        User user = mock(User.class);

        when(user.getPasswordHash())
                .thenReturn("hashed-password");

        when(userRepository.findByEmailIgnoreCase("hieu@example.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "wrong-password",
                "hashed-password"
        )).thenReturn(false);

        assertThrows(
                UnauthorizedException.class,
                () -> authService.login(request)
        );
    }
}