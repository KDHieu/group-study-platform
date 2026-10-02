package com.grouplearning.backend.controller;

import com.grouplearning.backend.dto.response.VideoTokenResponse;
import com.grouplearning.backend.service.LiveKitTokenService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.UUID;

@RestController
@RequestMapping(
        "/api/groups/{groupId}/video"
)
@SecurityRequirement(name = "bearerAuth")
public class VideoRoomController {

    private final LiveKitTokenService liveKitTokenService;

    public VideoRoomController(
            LiveKitTokenService liveKitTokenService
    ) {
        this.liveKitTokenService =
                liveKitTokenService;
    }

    @PostMapping("/token")
    public ResponseEntity<VideoTokenResponse>
    createToken(
            @PathVariable UUID groupId,
            Principal principal
    ) {
        UUID userId =
                extractUserId(principal);

        VideoTokenResponse response =
                liveKitTokenService
                        .createJoinToken(
                                groupId,
                                userId
                        );

        return ResponseEntity.ok(
                response
        );
    }

    private UUID extractUserId(
            Principal principal
    ) {
        if (!(principal
                instanceof JwtAuthenticationToken authentication)) {

            throw new IllegalStateException(
                    "Authenticated user required"
            );
        }

        try {
            return UUID.fromString(
                    authentication
                            .getToken()
                            .getSubject()
            );

        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException(
                    "Invalid authenticated user identifier",
                    exception
            );
        }
    }
}