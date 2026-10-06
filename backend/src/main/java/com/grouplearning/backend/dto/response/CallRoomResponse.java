package com.grouplearning.backend.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CallRoomResponse(
        UUID id,
        UUID groupId,
        String name,
        UUID createdById,
        String createdByUsername,
        Instant createdAt,
        List<CallRoomParticipantResponse> participants
) {
}