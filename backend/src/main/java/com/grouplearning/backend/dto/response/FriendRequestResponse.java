package com.grouplearning.backend.dto.response;

import com.grouplearning.backend.entity.FriendshipStatus;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(
        description = "Friend request information"
)
public record FriendRequestResponse(

        @Schema(
                description = "Unique identifier of the relationship"
        )
        UUID id,

        @Schema(
                description = "User who sent the friend request"
        )
        FriendUserResponse requester,

        @Schema(
                description = "User who received the friend request"
        )
        FriendUserResponse addressee,

        @Schema(
                description = "Current relationship status",
                example = "PENDING"
        )
        FriendshipStatus status,

        @Schema(
                description = "Time when the friend request was created"
        )
        Instant createdAt

) {
}