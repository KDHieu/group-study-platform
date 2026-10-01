package com.grouplearning.backend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(
        description = "Basic user information used in friendship responses"
)
public record FriendUserResponse(

        @Schema(
                description = "Unique identifier of the user"
        )
        UUID id,

        @Schema(
                description = "Unique username",
                example = "testuser"
        )
        String username,

        @Schema(
                description = "Display name",
                example = "Test User"
        )
        String displayName,

        @Schema(
                description = "API URL for retrieving the user's avatar",
                example = "/api/users/550e8400-e29b-41d4-a716-446655440000/avatar",
                nullable = true
        )
        String avatarUrl

) {
}