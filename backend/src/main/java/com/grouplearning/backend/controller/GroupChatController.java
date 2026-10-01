package com.grouplearning.backend.controller;

import com.grouplearning.backend.dto.response.ChatMessageResponse;
import com.grouplearning.backend.service.GroupChatService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/groups/{groupId}/messages")
@Tag(
        name = "Group Chat",
        description = "Study group chat APIs"
)
@SecurityRequirement(name = "bearerAuth")
public class GroupChatController {

    private final GroupChatService
            groupChatService;

    public GroupChatController(
            GroupChatService groupChatService
    ) {
        this.groupChatService =
                groupChatService;
    }

    @GetMapping
    @Operation(
            summary = "Get group chat history",
            description = "Returns paginated chat messages for a study group. The authenticated user must be a group member."
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
    public ResponseEntity<Page<ChatMessageResponse>>
    getMessageHistory(
            @PathVariable UUID groupId,

            @Parameter(hidden = true)
            @AuthenticationPrincipal Jwt jwt,

            @Parameter(hidden = true)
            @PageableDefault(
                    size = 30,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable
    ) {
        UUID userId =
                UUID.fromString(
                        jwt.getSubject()
                );

        return ResponseEntity.ok(
                groupChatService
                        .getMessageHistory(
                                groupId,
                                userId,
                                pageable
                        )
        );
    }
}