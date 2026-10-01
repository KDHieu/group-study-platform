package com.grouplearning.backend.dto.response;

import java.util.UUID;

public record TypingEventResponse(

        UUID groupId,

        UUID userId,

        String username,

        String displayName,

        boolean typing

) {
}