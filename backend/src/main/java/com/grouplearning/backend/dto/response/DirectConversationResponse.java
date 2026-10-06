package com.grouplearning.backend.dto.response;

import java.util.UUID;

public record DirectConversationResponse(
        UUID userId,
        String username,
        String displayName,
        String avatarUrl,
        DirectMessageResponse lastMessage
) {
}