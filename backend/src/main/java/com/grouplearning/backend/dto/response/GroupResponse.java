package com.grouplearning.backend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Study group information")
public record GroupResponse(

        @Schema(
                description = "Unique identifier of the study group",
                example = "550e8400-e29b-41d4-a716-446655440000"
        )
        UUID id,

        @Schema(
                description = "Name of the study group",
                example = "Distributed Systems Study Group"
        )
        String name,

        @Schema(
                description = "Description of the study group",
                example = "A group for students learning distributed systems.",
                nullable = true
        )
        String description,

        @Schema(
                description = "Unique identifier of the group owner",
                example = "6ba7b810-9dad-11d1-80b4-00c04fd430c8"
        )
        UUID ownerId,

        @Schema(
                description = "Username of the group owner",
                example = "hieu"
        )
        String ownerUsername,

        @Schema(
                description = "Time when the study group was created",
                example = "2026-10-01T03:00:00Z"
        )
        Instant createdAt

) {
}