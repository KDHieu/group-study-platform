package com.grouplearning.backend.dto.response;

import java.time.Instant;
import java.util.UUID;

public record DirectMessageResponse(
        UUID id,
        UUID senderId,
        UUID receiverId,
        String content,
        Instant createdAt
) {
}