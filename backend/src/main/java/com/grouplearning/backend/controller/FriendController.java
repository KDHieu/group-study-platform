package com.grouplearning.backend.controller;

import com.grouplearning.backend.dto.response.FriendResponse;
import com.grouplearning.backend.dto.response.FriendRequestResponse;
import com.grouplearning.backend.service.FriendService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/friends")
@Tag(
        name = "Friends",
        description = "Friend request and friendship management APIs"
)
@SecurityRequirement(name = "bearerAuth")
public class FriendController {

    private final FriendService friendService;

    public FriendController(
            FriendService friendService
    ) {
        this.friendService = friendService;
    }

    @PostMapping("/requests/{userId}")
    @Operation(
            summary = "Send friend request",
            description = "Sends a friend request to another user"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Friend request created"
            ),
            @ApiResponse(
                    responseCode = "401",
                    ref = "#/components/responses/Unauthorized"
            ),
            @ApiResponse(
                    responseCode = "404",
                    ref = "#/components/responses/NotFound"
            ),
            @ApiResponse(
                    responseCode = "409",
                    ref = "#/components/responses/Conflict"
            )
    })
    public ResponseEntity<FriendRequestResponse>
    sendRequest(
            @Parameter(hidden = true)
            @AuthenticationPrincipal Jwt jwt,

            @PathVariable UUID userId
    ) {
        UUID requesterId =
                UUID.fromString(
                        jwt.getSubject()
                );

        FriendRequestResponse response =
                friendService.sendRequest(
                        requesterId,
                        userId
                );

        return ResponseEntity
                .created(
                        URI.create(
                                "/api/friends/requests/"
                                        + response.id()
                        )
                )
                .body(response);
    }

    @GetMapping("/requests/incoming")
    @Operation(
            summary = "Get incoming friend requests",
            description = "Returns pending requests received by the authenticated user"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Incoming requests retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    ref = "#/components/responses/Unauthorized"
            )
    })
    public ResponseEntity<List<FriendRequestResponse>>
    getIncomingRequests(
            @Parameter(hidden = true)
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId =
                UUID.fromString(
                        jwt.getSubject()
                );

        return ResponseEntity.ok(
                friendService
                        .getIncomingRequests(
                                userId
                        )
        );
    }

    @GetMapping("/requests/outgoing")
    @Operation(
            summary = "Get outgoing friend requests",
            description = "Returns pending requests sent by the authenticated user"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Outgoing requests retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    ref = "#/components/responses/Unauthorized"
            )
    })
    public ResponseEntity<List<FriendRequestResponse>>
    getOutgoingRequests(
            @Parameter(hidden = true)
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId =
                UUID.fromString(
                        jwt.getSubject()
                );

        return ResponseEntity.ok(
                friendService
                        .getOutgoingRequests(
                                userId
                        )
        );
    }

    @PostMapping("/requests/{requestId}/accept")
    @Operation(
            summary = "Accept friend request",
            description = "Accepts a pending friend request received by the authenticated user"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Friend request accepted"
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
            ),
            @ApiResponse(
                    responseCode = "409",
                    ref = "#/components/responses/Conflict"
            )
    })
    public ResponseEntity<FriendRequestResponse>
    acceptRequest(
            @Parameter(hidden = true)
            @AuthenticationPrincipal Jwt jwt,

            @PathVariable UUID requestId
    ) {
        UUID userId =
                UUID.fromString(
                        jwt.getSubject()
                );

        return ResponseEntity.ok(
                friendService.acceptRequest(
                        userId,
                        requestId
                )
        );
    }

    @PostMapping("/requests/{requestId}/reject")
    @Operation(
            summary = "Reject friend request",
            description = "Rejects a pending friend request received by the authenticated user"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Friend request rejected"
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
            ),
            @ApiResponse(
                    responseCode = "409",
                    ref = "#/components/responses/Conflict"
            )
    })
    public ResponseEntity<Void> rejectRequest(
            @Parameter(hidden = true)
            @AuthenticationPrincipal Jwt jwt,

            @PathVariable UUID requestId
    ) {
        UUID userId =
                UUID.fromString(
                        jwt.getSubject()
                );

        friendService.rejectRequest(
                userId,
                requestId
        );

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/requests/{requestId}")
    @Operation(
            summary = "Cancel sent friend request",
            description = "Cancels a pending friend request sent by the authenticated user"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Friend request cancelled"
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
            ),
            @ApiResponse(
                    responseCode = "409",
                    ref = "#/components/responses/Conflict"
            )
    })
    public ResponseEntity<Void> cancelRequest(
            @Parameter(hidden = true)
            @AuthenticationPrincipal Jwt jwt,

            @PathVariable UUID requestId
    ) {
        UUID userId =
                UUID.fromString(
                        jwt.getSubject()
                );

        friendService.cancelRequest(
                userId,
                requestId
        );

        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(
            summary = "Get friend list",
            description = "Returns all accepted friends of the authenticated user"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Friend list retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    ref = "#/components/responses/Unauthorized"
            )
    })
    public ResponseEntity<List<FriendResponse>>
    getFriends(
            @Parameter(hidden = true)
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId =
                UUID.fromString(
                        jwt.getSubject()
                );

        return ResponseEntity.ok(
                friendService.getFriends(userId)
        );
    }

    @DeleteMapping("/{friendUserId}")
    @Operation(
            summary = "Remove friend",
            description = "Removes an accepted friendship with another user"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Friend removed successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    ref = "#/components/responses/Unauthorized"
            ),
            @ApiResponse(
                    responseCode = "404",
                    ref = "#/components/responses/NotFound"
            )
    })
    public ResponseEntity<Void> removeFriend(
            @Parameter(hidden = true)
            @AuthenticationPrincipal Jwt jwt,

            @PathVariable UUID friendUserId
    ) {
        UUID userId =
                UUID.fromString(
                        jwt.getSubject()
                );

        friendService.removeFriend(
                userId,
                friendUserId
        );

        return ResponseEntity.noContent().build();
    }
}