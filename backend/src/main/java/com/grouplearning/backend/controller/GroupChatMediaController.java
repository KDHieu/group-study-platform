package com.grouplearning.backend.controller;

import com.grouplearning.backend.dto.response.ChatMessageResponse;
import com.grouplearning.backend.service.GroupChatService;

import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.UUID;

@RestController
@RequestMapping("/api/groups/{groupId}/messages")
public class GroupChatMediaController {

    private final GroupChatService groupChatService;

    private final SimpMessagingTemplate messagingTemplate;

    public GroupChatMediaController(
            GroupChatService groupChatService,
            SimpMessagingTemplate messagingTemplate
    ) {
        this.groupChatService =
                groupChatService;

        this.messagingTemplate =
                messagingTemplate;
    }

    @PostMapping(
            path = "/audio",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ChatMessageResponse> uploadAudio(
            @PathVariable UUID groupId,

            @RequestPart("audio")
            MultipartFile audio,

            @RequestParam
            Integer durationMs,

            Principal principal
    ) {
        UUID userId =
                extractUserId(principal);

        ChatMessageResponse response =
                groupChatService
                        .sendAudioMessage(
                                groupId,
                                userId,
                                audio,
                                durationMs
                        );

        /*
         * REST handles the binary upload.
         * STOMP distributes the newly-created
         * message to all connected group members.
         */
        messagingTemplate.convertAndSend(
                "/topic/groups/"
                        + groupId
                        + "/messages",
                response
        );

        return ResponseEntity
                .status(201)
                .body(response);
    }

    @GetMapping(
            "/{messageId}/audio"
    )
    public ResponseEntity<byte[]> getAudio(
            @PathVariable UUID groupId,

            @PathVariable UUID messageId,

            Principal principal
    ) {
        UUID userId =
                extractUserId(principal);

        GroupChatService.ChatAudioContent audio =
                groupChatService
                        .getAudioContent(
                                groupId,
                                userId,
                                messageId
                        );

        MediaType mediaType;

        try {
            mediaType =
                    MediaType.parseMediaType(
                            audio.contentType()
                    );
        } catch (Exception exception) {
            mediaType =
                    MediaType.APPLICATION_OCTET_STREAM;
        }

        return ResponseEntity
                .ok()
                .contentType(mediaType)
                .cacheControl(
                        CacheControl.noCache()
                )
                .body(audio.content());
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