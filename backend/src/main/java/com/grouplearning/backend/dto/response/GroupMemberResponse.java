package com.grouplearning.backend.dto.response;

import com.grouplearning.backend.entity.GroupMemberRole;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Information about a member of a study group")
public record GroupMemberResponse(

        @Schema(
                description = "Unique identifier of the user",
                example = "550e8400-e29b-41d4-a716-446655440000"
        )
        UUID userId,

        @Schema(
                description = "Username of the group member",
                example = "member1"
        )
        String username,

        @Schema(
                description = "Role of the user inside the study group",
                example = "MEMBER",
                allowableValues = {
                        "OWNER",
                        "MEMBER"
                }
        )
        GroupMemberRole role,

        @Schema(
                description = "Time when the user joined the study group",
                example = "2026-10-01T03:30:00Z"
        )
        Instant joinedAt

) {
}