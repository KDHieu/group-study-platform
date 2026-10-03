package com.grouplearning.backend.service;

import com.grouplearning.backend.dto.request.LoginRequest;
import com.grouplearning.backend.dto.request.RegisterRequest;
import com.grouplearning.backend.dto.response.LoginResponse;
import com.grouplearning.backend.dto.response.UserResponse;
import com.grouplearning.backend.entity.User;
import com.grouplearning.backend.exception.ConflictException;
import com.grouplearning.backend.exception.UnauthorizedException;
import com.grouplearning.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final long ACCESS_TOKEN_EXPIRATION_SECONDS =
            3600L;

    private static final String TOKEN_ISSUER =
            "https://group-study-platform.local";

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtEncoder jwtEncoder;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService =
                new AuthService(
                        userRepository,
                        passwordEncoder,
                        jwtEncoder,
                        ACCESS_TOKEN_EXPIRATION_SECONDS
                );
    }

    // =========================================================
    // Registration
    // =========================================================

    @Test
    void register_shouldCreateUser_whenRequestIsValid() {
        RegisterRequest request =
                new RegisterRequest(
                        "hieu",
                        "hieu@example.com",
                        "12345678"
                );

        when(
                userRepository
                        .existsByUsernameIgnoreCase(
                                "hieu"
                        )
        ).thenReturn(false);

        when(
                userRepository
                        .existsByEmailIgnoreCase(
                                "hieu@example.com"
                        )
        ).thenReturn(false);

        when(
                passwordEncoder.encode(
                        "12345678"
                )
        ).thenReturn(
                "hashed-password"
        );

        User savedUser =
                mock(User.class);

        UUID userId =
                UUID.randomUUID();

        Instant createdAt =
                Instant.now();

        when(savedUser.getId())
                .thenReturn(userId);

        when(savedUser.getUsername())
                .thenReturn("hieu");

        when(savedUser.getEmail())
                .thenReturn(
                        "hieu@example.com"
                );

        when(savedUser.getCreatedAt())
                .thenReturn(createdAt);

        when(
                userRepository.save(
                        any(User.class)
                )
        ).thenReturn(savedUser);

        UserResponse response =
                authService.register(
                        request
                );

        assertEquals(
                userId,
                response.id()
        );

        assertEquals(
                "hieu",
                response.username()
        );

        assertEquals(
                "hieu@example.com",
                response.email()
        );

        assertEquals(
                createdAt,
                response.createdAt()
        );

        verify(passwordEncoder)
                .encode(
                        "12345678"
                );

        verify(userRepository)
                .save(
                        any(User.class)
                );
    }

    @Test
    void register_shouldNormalizeUsernameAndEmail() {
        RegisterRequest request =
                new RegisterRequest(
                        "  hieu  ",
                        "  HIEU@EXAMPLE.COM  ",
                        "12345678"
                );

        when(
                userRepository
                        .existsByUsernameIgnoreCase(
                                "hieu"
                        )
        ).thenReturn(false);

        when(
                userRepository
                        .existsByEmailIgnoreCase(
                                "hieu@example.com"
                        )
        ).thenReturn(false);

        when(
                passwordEncoder.encode(
                        "12345678"
                )
        ).thenReturn(
                "hashed-password"
        );

        User savedUser =
                mock(User.class);

        when(savedUser.getUsername())
                .thenReturn("hieu");

        when(savedUser.getEmail())
                .thenReturn(
                        "hieu@example.com"
                );

        when(
                userRepository.save(
                        any(User.class)
                )
        ).thenReturn(savedUser);

        authService.register(
                request
        );

        verify(userRepository)
                .existsByUsernameIgnoreCase(
                        "hieu"
                );

        verify(userRepository)
                .existsByEmailIgnoreCase(
                        "hieu@example.com"
                );
    }

    @Test
    void register_shouldThrowConflict_whenUsernameAlreadyExists() {
        RegisterRequest request =
                new RegisterRequest(
                        "hieu",
                        "hieu@example.com",
                        "12345678"
                );

        when(
                userRepository
                        .existsByUsernameIgnoreCase(
                                "hieu"
                        )
        ).thenReturn(true);

        assertThrows(
                ConflictException.class,
                () -> authService.register(
                        request
                )
        );

        verify(
                userRepository,
                never()
        ).save(
                any()
        );
    }

    @Test
    void register_shouldThrowConflict_whenEmailAlreadyExists() {
        RegisterRequest request =
                new RegisterRequest(
                        "hieu",
                        "hieu@example.com",
                        "12345678"
                );

        when(
                userRepository
                        .existsByUsernameIgnoreCase(
                                "hieu"
                        )
        ).thenReturn(false);

        when(
                userRepository
                        .existsByEmailIgnoreCase(
                                "hieu@example.com"
                        )
        ).thenReturn(true);

        assertThrows(
                ConflictException.class,
                () -> authService.register(
                        request
                )
        );

        verify(
                userRepository,
                never()
        ).save(
                any()
        );
    }

    // =========================================================
    // Login
    // =========================================================

    @Test
    void login_shouldReturnTokenAndUser_whenCredentialsAreValid() {
        UUID userId =
                UUID.randomUUID();

        Instant createdAt =
                Instant.now();

        User user =
                mock(User.class);

        when(user.getId())
                .thenReturn(userId);

        when(user.getUsername())
                .thenReturn("hieu");

        when(user.getEmail())
                .thenReturn(
                        "hieu@example.com"
                );

        when(user.getPasswordHash())
                .thenReturn(
                        "hashed-password"
                );

        when(user.getCreatedAt())
                .thenReturn(createdAt);

        when(
                userRepository
                        .findByEmailIgnoreCase(
                                "hieu@example.com"
                        )
        ).thenReturn(
                Optional.of(user)
        );

        when(
                passwordEncoder.matches(
                        "12345678",
                        "hashed-password"
                )
        ).thenReturn(true);

        Jwt encodedJwt =
                mock(Jwt.class);

        when(encodedJwt.getTokenValue())
                .thenReturn(
                        "test-jwt-token"
                );

        when(
                jwtEncoder.encode(
                        any(JwtEncoderParameters.class)
                )
        ).thenReturn(encodedJwt);

        LoginRequest request =
                new LoginRequest(
                        "hieu@example.com",
                        "12345678"
                );

        LoginResponse response =
                authService.login(
                        request
                );

        assertEquals(
                "test-jwt-token",
                response.accessToken()
        );

        assertEquals(
                "Bearer",
                response.tokenType()
        );

        assertEquals(
                ACCESS_TOKEN_EXPIRATION_SECONDS,
                response.expiresIn()
        );

        assertEquals(
                userId,
                response.user().id()
        );

        assertEquals(
                "hieu",
                response.user().username()
        );

        assertEquals(
                "hieu@example.com",
                response.user().email()
        );
    }

    @Test
    void login_shouldNormalizeEmail() {
        User user =
                mock(User.class);

        when(user.getId())
                .thenReturn(
                        UUID.randomUUID()
                );

        when(user.getUsername())
                .thenReturn("hieu");

        when(user.getEmail())
                .thenReturn(
                        "hieu@example.com"
                );

        when(user.getPasswordHash())
                .thenReturn(
                        "hashed-password"
                );

        when(
                userRepository
                        .findByEmailIgnoreCase(
                                "hieu@example.com"
                        )
        ).thenReturn(
                Optional.of(user)
        );

        when(
                passwordEncoder.matches(
                        "12345678",
                        "hashed-password"
                )
        ).thenReturn(true);

        Jwt encodedJwt =
                mock(Jwt.class);

        when(encodedJwt.getTokenValue())
                .thenReturn(
                        "test-jwt-token"
                );

        when(
                jwtEncoder.encode(
                        any(JwtEncoderParameters.class)
                )
        ).thenReturn(encodedJwt);

        LoginRequest request =
                new LoginRequest(
                        "  HIEU@EXAMPLE.COM  ",
                        "12345678"
                );

        authService.login(
                request
        );

        verify(userRepository)
                .findByEmailIgnoreCase(
                        "hieu@example.com"
                );
    }

    @Test
    void login_shouldThrowUnauthorized_whenEmailDoesNotExist() {
        LoginRequest request =
                new LoginRequest(
                        "missing@example.com",
                        "12345678"
                );

        when(
                userRepository
                        .findByEmailIgnoreCase(
                                "missing@example.com"
                        )
        ).thenReturn(
                Optional.empty()
        );

        assertThrows(
                UnauthorizedException.class,
                () -> authService.login(
                        request
                )
        );

        verify(
                passwordEncoder,
                never()
        ).matches(
                any(),
                any()
        );

        verify(
                jwtEncoder,
                never()
        ).encode(
                any()
        );
    }

    @Test
    void login_shouldThrowUnauthorized_whenPasswordIsInvalid() {
        LoginRequest request =
                new LoginRequest(
                        "hieu@example.com",
                        "wrong-password"
                );

        User user =
                mock(User.class);

        when(user.getPasswordHash())
                .thenReturn(
                        "hashed-password"
                );

        when(
                userRepository
                        .findByEmailIgnoreCase(
                                "hieu@example.com"
                        )
        ).thenReturn(
                Optional.of(user)
        );

        when(
                passwordEncoder.matches(
                        "wrong-password",
                        "hashed-password"
                )
        ).thenReturn(false);

        assertThrows(
                UnauthorizedException.class,
                () -> authService.login(
                        request
                )
        );

        verify(
                jwtEncoder,
                never()
        ).encode(
                any()
        );
    }

    // =========================================================
    // JWT generation
    // =========================================================

    @Test
    void login_shouldGenerateTokenWithExpectedClaims() {
        UUID userId =
                UUID.randomUUID();

        User user =
                createLoginUser(
                        userId
                );

        Jwt encodedJwt =
                mock(Jwt.class);

        when(encodedJwt.getTokenValue())
                .thenReturn(
                        "test-jwt-token"
                );

        when(
                jwtEncoder.encode(
                        any(JwtEncoderParameters.class)
                )
        ).thenReturn(encodedJwt);

        authService.login(
                new LoginRequest(
                        "hieu@example.com",
                        "12345678"
                )
        );

        ArgumentCaptor<JwtEncoderParameters> captor =
                ArgumentCaptor.forClass(
                        JwtEncoderParameters.class
                );

        verify(jwtEncoder)
                .encode(
                        captor.capture()
                );

        JwtClaimsSet claims =
                captor.getValue()
                        .getClaims();

        assertEquals(
                TOKEN_ISSUER,
                claims.getIssuer().toString()
        );

        assertEquals(
                userId.toString(),
                claims.getSubject()
        );

        assertEquals(
                "hieu",
                claims.getClaim("username")
        );

        assertTrue(
                claims.getIssuedAt() != null
        );

        assertTrue(
                claims.getExpiresAt() != null
        );

        assertTrue(
                claims.getExpiresAt()
                        .isAfter(
                                claims.getIssuedAt()
                        )
        );
    }

    @Test
    void login_shouldGenerateTokenWithConfiguredExpiration() {
        User user =
                createLoginUser(
                        UUID.randomUUID()
                );

        Jwt encodedJwt =
                mock(Jwt.class);

        when(encodedJwt.getTokenValue())
                .thenReturn(
                        "test-jwt-token"
                );

        when(
                jwtEncoder.encode(
                        any(JwtEncoderParameters.class)
                )
        ).thenReturn(encodedJwt);

        authService.login(
                new LoginRequest(
                        "hieu@example.com",
                        "12345678"
                )
        );

        ArgumentCaptor<JwtEncoderParameters> captor =
                ArgumentCaptor.forClass(
                        JwtEncoderParameters.class
                );

        verify(jwtEncoder)
                .encode(
                        captor.capture()
                );

        JwtClaimsSet claims =
                captor.getValue()
                        .getClaims();

        long expirationSeconds =
                Duration.between(
                        claims.getIssuedAt(),
                        claims.getExpiresAt()
                ).getSeconds();

        assertEquals(
                ACCESS_TOKEN_EXPIRATION_SECONDS,
                expirationSeconds
        );
    }

    // =========================================================
    // Test helpers
    // =========================================================

    private User createLoginUser(
            UUID userId
    ) {
        User user =
                mock(User.class);

        when(user.getId())
                .thenReturn(userId);

        when(user.getUsername())
                .thenReturn("hieu");

        when(user.getEmail())
                .thenReturn(
                        "hieu@example.com"
                );

        when(user.getPasswordHash())
                .thenReturn(
                        "hashed-password"
                );

        when(
                userRepository
                        .findByEmailIgnoreCase(
                                "hieu@example.com"
                        )
        ).thenReturn(
                Optional.of(user)
        );

        when(
                passwordEncoder.matches(
                        "12345678",
                        "hashed-password"
                )
        ).thenReturn(true);

        return user;
    }
}