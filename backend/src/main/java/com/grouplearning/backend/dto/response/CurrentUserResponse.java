package com.grouplearning.backend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Basic information about the currently authenticated user")
public record CurrentUserResponse(

        @Schema(
                description = "Unique identifier of the authenticated user",
                example = "550e8400-e29b-41d4-a716-446655440000"
        )
        UUID id,

        @Schema(
                description = "Username of the authenticated user",
                example = "hieu"
        )
        String username

) {
}