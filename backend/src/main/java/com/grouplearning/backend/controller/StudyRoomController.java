package com.grouplearning.backend.controller;

import com.grouplearning.backend.dto.request.CreateCallRoomRequest;
import com.grouplearning.backend.dto.response.CallRoomResponse;
import com.grouplearning.backend.dto.response.ChatMessageResponse;
import com.grouplearning.backend.dto.response.VideoTokenResponse;
import com.grouplearning.backend.service.StudyRoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/groups/{groupId}")
@Tag(
        name = "Study Rooms",
        description = "Study room chat, media, call room and video APIs"
)
@SecurityRequirement(name = "bearerAuth")
public class StudyRoomController {

    private final StudyRoomService studyRoomService;

    private final SimpMessagingTemplate messagingTemplate;

    public StudyRoomController(
            StudyRoomService studyRoomService,
            SimpMessagingTemplate messagingTemplate
    ) {
        this.studyRoomService =
                studyRoomService;

        this.messagingTemplate =
                messagingTemplate;
    }

    // =========================================================
    // Chat history
    // =========================================================

    @GetMapping("/messages")
    @Operation(
            summary = "Get study room chat history",
            description = """
                    Returns paginated chat messages for a study group.

                    The authenticated user must be a member of the group.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Message history retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    ref = "#/components/responses/Unauthorized"
            ),
            @ApiResponse(
                    responseCode = "403",
                    ref = "#/components/responses/Forbidden"
            ),
            @ApiResponse(
                    responseCode = "404",
                    ref = "#/components/responses/NotFound"
            )
    })
    public ResponseEntity<Page<ChatMessageResponse>> getMessageHistory(
            @PathVariable UUID groupId,

            Principal principal,

            @Parameter(hidden = true)
            @PageableDefault(
                    size = 30,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable
    ) {
        UUID userId =
                extractUserId(principal);

        Page<ChatMessageResponse> response =
                studyRoomService.getMessageHistory(
                        groupId,
                        userId,
                        pageable
                );

        return ResponseEntity.ok(
                response
        );
    }

    // =========================================================
    // Voice messages
    // =========================================================

    @PostMapping(
            path = "/messages/audio",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @Operation(
            summary = "Send voice message",
            description = """
                    Uploads an audio file and creates a voice message
                    in the study room.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Voice message created successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    ref = "#/components/responses/BadRequest"
            ),
            @ApiResponse(
                    responseCode = "401",
                    ref = "#/components/responses/Unauthorized"
            ),
            @ApiResponse(
                    responseCode = "403",
                    ref = "#/components/responses/Forbidden"
            ),
            @ApiResponse(
                    responseCode = "404",
                    ref = "#/components/responses/NotFound"
            )
    })
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
                studyRoomService.sendAudioMessage(
                        groupId,
                        userId,
                        audio,
                        durationMs
                );

        /*
         * Binary upload is handled through REST.
         * The created message is then broadcast
         * to connected room members through STOMP.
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
            "/messages/{messageId}/audio"
    )
    @Operation(
            summary = "Get voice message audio",
            description = """
                    Returns the audio content of a voice message.

                    The authenticated user must be a member
                    of the study group.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Audio retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    ref = "#/components/responses/Unauthorized"
            ),
            @ApiResponse(
                    responseCode = "403",
                    ref = "#/components/responses/Forbidden"
            ),
            @ApiResponse(
                    responseCode = "404",
                    ref = "#/components/responses/NotFound"
            )
    })
    public ResponseEntity<byte[]> getAudio(
            @PathVariable UUID groupId,

            @PathVariable UUID messageId,

            Principal principal
    ) {
        UUID userId =
                extractUserId(principal);

        StudyRoomService.ChatAudioContent audio =
                studyRoomService.getAudioContent(
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
                .body(
                        audio.content()
                );
    }

    // =========================================================
    // Call rooms
    // =========================================================

    @GetMapping("/call-rooms")
    @Operation(
            summary = "Get call rooms",
            description = """
                    Returns the call rooms that belong to this study group.

                    The authenticated user must be a member of the group.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Call rooms retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    ref = "#/components/responses/Unauthorized"
            ),
            @ApiResponse(
                    responseCode = "403",
                    ref = "#/components/responses/Forbidden"
            ),
            @ApiResponse(
                    responseCode = "404",
                    ref = "#/components/responses/NotFound"
            )
    })
    public ResponseEntity<List<CallRoomResponse>> getCallRooms(
            @PathVariable UUID groupId,

            Principal principal
    ) {
        UUID userId =
                extractUserId(principal);

        List<CallRoomResponse> response =
                studyRoomService.getCallRooms(
                        groupId,
                        userId
                );

        return ResponseEntity.ok(
                response
        );
    }

    @PostMapping("/call-rooms")
    @Operation(
            summary = "Create call room",
            description = """
                    Creates a new call room inside this study group.

                    Any authenticated member of the study group
                    may create a call room.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Call room created successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    ref = "#/components/responses/BadRequest"
            ),
            @ApiResponse(
                    responseCode = "401",
                    ref = "#/components/responses/Unauthorized"
            ),
            @ApiResponse(
                    responseCode = "403",
                    ref = "#/components/responses/Forbidden"
            ),
            @ApiResponse(
                    responseCode = "404",
                    ref = "#/components/responses/NotFound"
            )
    })
    public ResponseEntity<CallRoomResponse> createCallRoom(
            @PathVariable UUID groupId,

            @Valid
            @RequestBody
            CreateCallRoomRequest request,

            Principal principal
    ) {
        UUID userId =
                extractUserId(principal);

        CallRoomResponse response =
                studyRoomService.createCallRoom(
                        groupId,
                        userId,
                        request
                );

        return ResponseEntity
                .status(201)
                .body(response);
    }

    @PostMapping(
            "/call-rooms/{callRoomId}/token"
    )
    @Operation(
            summary = "Create call room join token",
            description = """
                    Creates a temporary LiveKit token for one specific
                    call room inside this study group.

                    Different call room IDs map to different LiveKit rooms.
                    The authenticated user must be a study group member.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Call room token created successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    ref = "#/components/responses/Unauthorized"
            ),
            @ApiResponse(
                    responseCode = "403",
                    ref = "#/components/responses/Forbidden"
            ),
            @ApiResponse(
                    responseCode = "404",
                    ref = "#/components/responses/NotFound"
            )
    })
    public ResponseEntity<VideoTokenResponse> createCallRoomToken(
            @PathVariable UUID groupId,

            @PathVariable UUID callRoomId,

            Principal principal
    ) {
        UUID userId =
                extractUserId(principal);

        VideoTokenResponse response =
                studyRoomService
                        .createCallRoomJoinToken(
                                groupId,
                                callRoomId,
                                userId
                        );

        return ResponseEntity.ok(
                response
        );
    }

    // =========================================================
    // Authentication helper
    // =========================================================

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
