package com.grouplearning.backend.controller;

import com.grouplearning.backend.dto.request.SendDirectMessageRequest;
import com.grouplearning.backend.dto.response.DirectConversationResponse;
import com.grouplearning.backend.dto.response.DirectMessageResponse;
import com.grouplearning.backend.service.DirectMessageService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/messages")
@Tag(
        name = "Direct Messages",
        description = "Private messaging between accepted friends"
)
@SecurityRequirement(name = "bearerAuth")
public class DirectMessageController {

    private final DirectMessageService directMessageService;

    private final SimpMessagingTemplate messagingTemplate;

    public DirectMessageController(
            DirectMessageService directMessageService,
            SimpMessagingTemplate messagingTemplate
    ) {
        this.directMessageService =
                directMessageService;

        this.messagingTemplate =
                messagingTemplate;
    }

    @GetMapping("/conversations")
    @Operation(
            summary = "Get direct message conversations"
    )
    public ResponseEntity<List<DirectConversationResponse>> getConversations(
            Principal principal
    ) {
        UUID userId =
                extractUserId(principal);

        return ResponseEntity.ok(
                directMessageService
                        .getConversations(
                                userId
                        )
        );
    }

    @GetMapping("/{userId}")
    @Operation(
            summary = "Get direct message history with a friend"
    )
    public ResponseEntity<Page<DirectMessageResponse>> getConversation(
            @PathVariable
            UUID userId,

            @Parameter(hidden = true)
            @PageableDefault(size = 30)
            Pageable pageable,

            Principal principal
    ) {
        UUID authenticatedUserId =
                extractUserId(principal);

        return ResponseEntity.ok(
                directMessageService
                        .getConversation(
                                authenticatedUserId,
                                userId,
                                pageable
                        )
        );
    }

    @PostMapping("/{userId}")
    @Operation(
            summary = "Send a direct message to a friend"
    )
    public ResponseEntity<DirectMessageResponse> sendMessage(
            @PathVariable
            UUID userId,

            @Valid
            @RequestBody
            SendDirectMessageRequest request,

            Principal principal
    ) {
        UUID senderId =
                extractUserId(principal);

        DirectMessageResponse response =
                directMessageService
                        .sendMessage(
                                senderId,
                                userId,
                                request
                        );

        /*
         * Push the persisted message to the receiver.
         */
        messagingTemplate
                .convertAndSendToUser(
                        userId.toString(),
                        "/queue/messages",
                        response
                );

        /*
         * Also publish it to the sender so other open
         * tabs/devices of the same account stay in sync.
         */
        messagingTemplate
                .convertAndSendToUser(
                        senderId.toString(),
                        "/queue/messages",
                        response
                );

        return ResponseEntity
                .status(201)
                .body(response);
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

        String subject =
                authentication
                        .getToken()
                        .getSubject();

        try {
            return UUID.fromString(
                    subject
            );

        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException(
                    "Invalid authenticated user identifier",
                    exception
            );
        }
    }
}