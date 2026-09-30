package com.grouplearning.backend.service;

import com.grouplearning.backend.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class JwtServiceTest {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private JwtDecoder jwtDecoder;

    @Test
    void generateAccessToken_shouldContainExpectedClaims() {

        UUID userId = UUID.randomUUID();

        User user = mock(User.class);

        when(user.getId()).thenReturn(userId);
        when(user.getUsername()).thenReturn("hieu");

        String token =
                jwtService.generateAccessToken(user);

        assertNotNull(token);
        assertFalse(token.isBlank());

        Jwt jwt = jwtDecoder.decode(token);

        assertEquals(
                userId.toString(),
                jwt.getSubject()
        );

        assertEquals(
                "hieu",
                jwt.getClaimAsString("username")
        );

        assertEquals(
                "https://group-study-platform.local",
                jwt.getIssuer().toString()
        );

        assertNotNull(jwt.getIssuedAt());
        assertNotNull(jwt.getExpiresAt());

        assertTrue(
                jwt.getExpiresAt()
                        .isAfter(jwt.getIssuedAt())
        );
    }

    @Test
    void generateAccessToken_shouldUseConfiguredExpiration() {

        User user = mock(User.class);

        when(user.getId()).thenReturn(UUID.randomUUID());
        when(user.getUsername()).thenReturn("hieu");

        String token =
                jwtService.generateAccessToken(user);

        Jwt jwt = jwtDecoder.decode(token);

        long expirationSeconds =
                jwt.getExpiresAt().getEpochSecond()
                        - jwt.getIssuedAt().getEpochSecond();

        assertEquals(
                jwtService.getAccessTokenExpirationSeconds(),
                expirationSeconds
        );
    }
}