package com.grouplearning.backend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(
        description = "Basic public user information returned by user search"
)
public record UserSearchResponse(

        @Schema(
                description = "Unique identifier of the user"
        )
        UUID id,

        @Schema(
                description = "Unique username",
                example = "kdhieu"
        )
        String username,

        @Schema(
                description = "Display name",
                example = "Kiều Đăng Hiếu"
        )
        String displayName,

        @Schema(
                description = "Short user biography",
                nullable = true
        )
        String bio,

        @Schema(
                description = "API URL for retrieving the user's avatar",
                example = "/api/users/550e8400-e29b-41d4-a716-446655440000/avatar",
                nullable = true
        )
        String avatarUrl

) {
}