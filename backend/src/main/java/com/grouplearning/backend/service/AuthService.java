package com.grouplearning.backend.service;

import com.grouplearning.backend.dto.request.LoginRequest;
import com.grouplearning.backend.dto.request.RegisterRequest;
import com.grouplearning.backend.dto.response.LoginResponse;
import com.grouplearning.backend.dto.response.UserResponse;
import com.grouplearning.backend.entity.User;
import com.grouplearning.backend.exception.ConflictException;
import com.grouplearning.backend.exception.UnauthorizedException;
import com.grouplearning.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Locale;

@Service
public class AuthService {

    private static final String TOKEN_ISSUER =
            "https://group-study-platform.local";

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtEncoder jwtEncoder;

    private final long accessTokenExpirationSeconds;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtEncoder jwtEncoder,
            @Value("${app.jwt.access-token-expiration-seconds}")
            long accessTokenExpirationSeconds
    ) {
        this.userRepository =
                userRepository;

        this.passwordEncoder =
                passwordEncoder;

        this.jwtEncoder =
                jwtEncoder;

        this.accessTokenExpirationSeconds =
                accessTokenExpirationSeconds;
    }

    // =========================================================
    // Registration
    // =========================================================

    @Transactional
    public UserResponse register(
            RegisterRequest request
    ) {
        String username =
                request.username()
                        .trim();

        String email =
                request.email()
                        .trim()
                        .toLowerCase(
                                Locale.ROOT
                        );

        if (userRepository
                .existsByUsernameIgnoreCase(
                        username
                )) {

            throw new ConflictException(
                    "Username already exists"
            );
        }

        if (userRepository
                .existsByEmailIgnoreCase(
                        email
                )) {

            throw new ConflictException(
                    "Email already exists"
            );
        }

        String passwordHash =
                passwordEncoder.encode(
                        request.password()
                );

        User user =
                new User(
                        username,
                        email,
                        passwordHash
                );

        User savedUser =
                userRepository.save(
                        user
                );

        return toUserResponse(
                savedUser
        );
    }

    // =========================================================
    // Login
    // =========================================================

    @Transactional(readOnly = true)
    public LoginResponse login(
            LoginRequest request
    ) {
        String email =
                request.email()
                        .trim()
                        .toLowerCase(
                                Locale.ROOT
                        );

        User user =
                userRepository
                        .findByEmailIgnoreCase(
                                email
                        )
                        .orElseThrow(() ->
                                new UnauthorizedException(
                                        "Invalid email or password"
                                )
                        );

        boolean passwordMatches =
                passwordEncoder.matches(
                        request.password(),
                        user.getPasswordHash()
                );

        if (!passwordMatches) {
            throw new UnauthorizedException(
                    "Invalid email or password"
            );
        }

        String accessToken =
                generateAccessToken(
                        user
                );

        return new LoginResponse(
                accessToken,
                "Bearer",
                accessTokenExpirationSeconds,
                toUserResponse(user)
        );
    }

    // =========================================================
    // JWT
    // =========================================================

    private String generateAccessToken(
            User user
    ) {
        Instant now =
                Instant.now();

        JwtClaimsSet claims =
                JwtClaimsSet.builder()
                        .issuer(
                                TOKEN_ISSUER
                        )
                        .issuedAt(
                                now
                        )
                        .expiresAt(
                                now.plusSeconds(
                                        accessTokenExpirationSeconds
                                )
                        )
                        .subject(
                                user.getId()
                                        .toString()
                        )
                        .claim(
                                "username",
                                user.getUsername()
                        )
                        .build();

        JwsHeader headers =
                JwsHeader
                        .with(
                                MacAlgorithm.HS256
                        )
                        .type(
                                "JWT"
                        )
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

    // =========================================================
    // Response mapping
    // =========================================================

    private UserResponse toUserResponse(
            User user
    ) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getCreatedAt()
        );
    }
}