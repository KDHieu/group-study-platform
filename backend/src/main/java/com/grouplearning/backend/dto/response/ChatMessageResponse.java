package com.grouplearning.backend.dto.response;

import com.grouplearning.backend.entity.ChatMessageType;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(
        description = "A message in a study group chat"
)
public record ChatMessageResponse(

        @Schema(
                description = "Unique identifier of the message"
        )
        UUID id,

        @Schema(
                description = "Study group containing this message"
        )
        UUID groupId,

        @Schema(
                description = "Information about the message sender"
        )
        ChatSenderResponse sender,

        @Schema(
                description = "Message type",
                example = "TEXT"
        )
        ChatMessageType type,

        @Schema(
                description = "Text content of the message",
                nullable = true
        )
        String content,

        @Schema(
                description = "Stored media object or URL for media messages",
                nullable = true
        )
        String mediaUrl,

        @Schema(
                description = "Time when the message was sent"
        )
        Instant createdAt

) {
}