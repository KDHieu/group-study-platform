package com.grouplearning.backend.dto.response;

public record VideoTokenResponse(

        String serverUrl,

        String token,

        String roomName,

        String participantIdentity

) {
}