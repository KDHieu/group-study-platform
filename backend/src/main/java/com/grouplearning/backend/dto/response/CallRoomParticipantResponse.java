package com.grouplearning.backend.dto.response;

public record CallRoomParticipantResponse(
        String sid,
        String identity,
        String name
) {
}