package com.grouplearning.backend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Public profile information of a user")
public record UserProfileResponse(

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
                description = "Display name shown to other users",
                example = "Kiều Đăng Hiếu"
        )
        String displayName,

        @Schema(
                description = "Short biography of the user",
                example = "IT student interested in Software Engineering and AI.",
                nullable = true
        )
        String bio,

        @Schema(
                description = "API URL used to retrieve the user's avatar",
                example = "/api/users/550e8400-e29b-41d4-a716-446655440000/avatar",
                nullable = true
        )
        String avatarUrl

) {
}