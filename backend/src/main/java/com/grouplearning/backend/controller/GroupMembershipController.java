package com.grouplearning.backend.controller;

import com.grouplearning.backend.dto.response.GroupMemberResponse;
import com.grouplearning.backend.service.GroupMembershipService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/groups")
@Tag(
        name = "Group Memberships",
        description = "Operations for joining, leaving and viewing members of study groups"
)
@SecurityRequirement(name = "bearerAuth")
public class GroupMembershipController {

    private final GroupMembershipService groupMembershipService;

    public GroupMembershipController(
            GroupMembershipService groupMembershipService
    ) {
        this.groupMembershipService = groupMembershipService;
    }

    @Operation(
            summary = "Join a study group",
            description = """
                    Adds the currently authenticated user to the study group
                    with the MEMBER role.

                    A user cannot join the same group more than once.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Joined the study group successfully"
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
    @PostMapping("/{groupId}/join")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void joinGroup(

            @Parameter(hidden = true)
            @AuthenticationPrincipal Jwt jwt,

            @Parameter(
                    description = "Unique identifier of the study group",
                    example = "550e8400-e29b-41d4-a716-446655440000"
            )
            @PathVariable UUID groupId
    ) {

        UUID currentUserId =
                UUID.fromString(jwt.getSubject());

        groupMembershipService.joinGroup(
                groupId,
                currentUserId
        );
    }

    @Operation(
            summary = "Leave a study group",
            description = """
                    Removes the currently authenticated user from the study group.

                    The group owner cannot leave the group because
                    every study group must have an owner.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Left the study group successfully"
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
    @DeleteMapping("/{groupId}/members/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void leaveGroup(

            @Parameter(hidden = true)
            @AuthenticationPrincipal Jwt jwt,

            @Parameter(
                    description = "Unique identifier of the study group",
                    example = "550e8400-e29b-41d4-a716-446655440000"
            )
            @PathVariable UUID groupId
    ) {

        UUID currentUserId =
                UUID.fromString(jwt.getSubject());

        groupMembershipService.leaveGroup(
                groupId,
                currentUserId
        );
    }

    @Operation(
            summary = "Get study group members",
            description = """
                    Returns all members of the specified study group,
                    including username, role and join time.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Group members retrieved successfully"
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
    @GetMapping("/{groupId}/members")
    public List<GroupMemberResponse> getMembers(

            @Parameter(
                    description = "Unique identifier of the study group",
                    example = "550e8400-e29b-41d4-a716-446655440000"
            )
            @PathVariable UUID groupId
    ) {

        return groupMembershipService.getMembers(
                groupId
        );
    }
}