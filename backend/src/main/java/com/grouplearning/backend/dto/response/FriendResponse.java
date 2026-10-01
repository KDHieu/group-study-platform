package com.grouplearning.backend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(
        description = "Information about one of the authenticated user's friends"
)
public record FriendResponse(

        @Schema(
                description = "Unique identifier of the friendship relationship"
        )
        UUID relationshipId,

        @Schema(
                description = "The other user in this friendship"
        )
        FriendUserResponse friend,

        @Schema(
                description = "Time when the friendship was accepted"
        )
        Instant friendsSince

) {
}