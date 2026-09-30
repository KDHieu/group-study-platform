package com.grouplearning.backend.dto.response;

import java.time.Instant;
import java.util.UUID;

public record GroupResponse(
        UUID id,
        String name,
        String description,
        UUID ownerId,
        String ownerUsername,
        Instant createdAt
) {
}