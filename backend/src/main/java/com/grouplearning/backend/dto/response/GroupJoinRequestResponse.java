package com.grouplearning.backend.dto.response;

import com.grouplearning.backend.entity.GroupJoinRequestStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(
        description = "Join request for a private study group"
)
public record GroupJoinRequestResponse(
        UUID id,
        UUID groupId,
        UUID userId,
        String username,
        GroupJoinRequestStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}