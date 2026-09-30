package com.grouplearning.backend.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class JwtSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtEncoder jwtEncoder;

    @Test
    void me_shouldReturnUnauthorized_whenTokenIsMissing()
            throws Exception {

        mockMvc.perform(
                        get("/api/auth/me")
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void me_shouldReturnCurrentUser_whenTokenIsValid()
            throws Exception {

        UUID userId = UUID.randomUUID();

        String token = createToken(
                userId,
                "hieu",
                Instant.now(),
                Instant.now().plusSeconds(3600)
        );

        mockMvc.perform(
                        get("/api/auth/me")
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(userId.toString()))
                .andExpect(jsonPath("$.username")
                        .value("hieu"));
    }

    @Test
    void me_shouldReturnUnauthorized_whenTokenIsExpired()
            throws Exception {

        UUID userId = UUID.randomUUID();

        Instant issuedAt =
                Instant.now().minusSeconds(7200);

        Instant expiredAt =
                Instant.now().minusSeconds(3600);

        String token = createToken(
                userId,
                "hieu",
                issuedAt,
                expiredAt
        );

        mockMvc.perform(
                        get("/api/auth/me")
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isUnauthorized());
    }

    private String createToken(
            UUID userId,
            String username,
            Instant issuedAt,
            Instant expiresAt
    ) {

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("https://group-study-platform.local")
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .subject(userId.toString())
                .claim("username", username)
                .build();

        JwsHeader headers = JwsHeader
                .with(MacAlgorithm.HS256)
                .type("JWT")
                .build();

        return jwtEncoder
                .encode(
                        JwtEncoderParameters.from(
                                headers,
                                claims
                        )
                )
                .getTokenValue();
    }

    @Test
    void me_shouldReturnUnauthorized_whenTokenIsInvalid()
            throws Exception {

        String invalidToken =
                "eyJhbGciOiJIUzI1NiJ9.invalid.signature";

        mockMvc.perform(
                        get("/api/auth/me")
                                .header(
                                        HttpHeaders.AUTHORIZATION,
                                        "Bearer " + invalidToken
                                )
                )
                .andExpect(status().isUnauthorized());
    }
}