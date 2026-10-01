package com.grouplearning.backend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(
        description = "Basic information about the sender of a chat message"
)
public record ChatSenderResponse(

        @Schema(
                description = "Unique identifier of the sender"
        )
        UUID id,

        @Schema(
                description = "Username of the sender",
                example = "testuser"
        )
        String username,

        @Schema(
                description = "Display name of the sender",
                example = "Test User"
        )
        String displayName,

        @Schema(
                description = "API URL for retrieving the sender's avatar",
                nullable = true
        )
        String avatarUrl

) {
}