package com.grouplearning.backend.service;

import com.grouplearning.backend.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class JwtService {

    private final JwtEncoder jwtEncoder;
    private final long accessTokenExpirationSeconds;

    public JwtService(
            JwtEncoder jwtEncoder,
            @Value("${app.jwt.access-token-expiration-seconds}")
            long accessTokenExpirationSeconds
    ) {
        this.jwtEncoder = jwtEncoder;
        this.accessTokenExpirationSeconds =
                accessTokenExpirationSeconds;
    }

    public String generateAccessToken(User user) {

        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("https://group-study-platform.local")
                .issuedAt(now)
                .expiresAt(
                        now.plusSeconds(
                                accessTokenExpirationSeconds
                        )
                )
                .subject(user.getId().toString())
                .claim("username", user.getUsername())
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

    public long getAccessTokenExpirationSeconds() {
        return accessTokenExpirationSeconds;
    }
}