package com.grouplearning.backend.controller;

import com.grouplearning.backend.dto.request.SendChatMessageRequest;
import com.grouplearning.backend.dto.request.TypingEventRequest;
import com.grouplearning.backend.dto.response.ChatMessageResponse;
import com.grouplearning.backend.dto.response.TypingEventResponse;
import com.grouplearning.backend.service.GroupChatService;

import jakarta.validation.Valid;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.UUID;

@Controller
public class GroupChatWebSocketController {

    private final GroupChatService groupChatService;

    private final SimpMessagingTemplate messagingTemplate;

    public GroupChatWebSocketController(
            GroupChatService groupChatService,
            SimpMessagingTemplate messagingTemplate
    ) {
        this.groupChatService =
                groupChatService;

        this.messagingTemplate =
                messagingTemplate;
    }

    @MessageMapping(
            "/groups/{groupId}/messages"
    )
    public void sendMessage(
            @DestinationVariable UUID groupId,

            @Valid
            SendChatMessageRequest request,

            Principal principal
    ) {
        UUID userId =
                extractUserId(principal);

        ChatMessageResponse response =
                groupChatService
                        .sendTextMessage(
                                groupId,
                                userId,
                                request.content()
                        );

        messagingTemplate.convertAndSend(
                "/topic/groups/"
                        + groupId
                        + "/messages",
                response
        );
    }

    @MessageMapping(
            "/groups/{groupId}/typing"
    )
    public void typing(
            @DestinationVariable UUID groupId,

            @Valid
            TypingEventRequest request,

            Principal principal
    ) {
        UUID userId =
                extractUserId(principal);

        TypingEventResponse response =
                groupChatService
                        .createTypingEvent(
                                groupId,
                                userId,
                                request.typing()
                        );

        messagingTemplate.convertAndSend(
                "/topic/groups/"
                        + groupId
                        + "/typing",
                response
        );
    }

    private UUID extractUserId(
            Principal principal
    ) {
        if (!(principal
                instanceof JwtAuthenticationToken authentication)) {

            throw new IllegalStateException(
                    "Authenticated WebSocket user required"
            );
        }

        String subject =
                authentication
                        .getToken()
                        .getSubject();

        try {
            return UUID.fromString(subject);

        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException(
                    "Invalid authenticated user identifier",
                    exception
            );
        }
    }
}