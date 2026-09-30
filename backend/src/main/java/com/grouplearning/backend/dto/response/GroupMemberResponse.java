package com.grouplearning.backend.dto.response;

import com.grouplearning.backend.entity.GroupMemberRole;

import java.time.Instant;
import java.util.UUID;

public record GroupMemberResponse(
        UUID userId,
        String username,
        GroupMemberRole role,
        Instant joinedAt
) {
}