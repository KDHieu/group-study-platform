package com.grouplearning.backend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Public information about a user")
public record UserResponse(

        @Schema(
                description = "Unique identifier of the user",
                example = "550e8400-e29b-41d4-a716-446655440000"
        )
        UUID id,

        @Schema(
                description = "Unique username of the user",
                example = "hieu"
        )
        String username,

        @Schema(
                description = "Email address of the user",
                example = "hieu@example.com"
        )
        String email,

        @Schema(
                description = "Time when the user account was created",
                example = "2026-10-01T03:00:00Z"
        )
        Instant createdAt

) {
}