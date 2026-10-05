package com.grouplearning.backend.controller;

import com.grouplearning.backend.dto.request.CreateGroupRequest;
import com.grouplearning.backend.dto.response.GroupJoinRequestResponse;
import com.grouplearning.backend.dto.response.GroupMemberResponse;
import com.grouplearning.backend.dto.response.GroupResponse;
import com.grouplearning.backend.service.StudyGroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/groups")
@Tag(
        name = "Study Groups",
        description = "Study group management, discovery and membership APIs"
)
@SecurityRequirement(name = "bearerAuth")
public class StudyGroupController {

    private final StudyGroupService studyGroupService;

    public StudyGroupController(
            StudyGroupService studyGroupService
    ) {
        this.studyGroupService =
                studyGroupService;
    }

    @Operation(
            summary = "Create a study group",
            description = """
                    Creates a PUBLIC or PRIVATE study group.

                    The authenticated user automatically becomes
                    the OWNER and is added as an OWNER member.
                    """
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GroupResponse createGroup(
            @Parameter(hidden = true)
            @AuthenticationPrincipal Jwt jwt,

            @Valid
            @RequestBody CreateGroupRequest request
    ) {
        UUID currentUserId =
                UUID.fromString(
                        jwt.getSubject()
                );

        return studyGroupService.createGroup(
                currentUserId,
                request
        );
    }

    @Operation(
            summary = "Discover or search study groups",
            description = """
                    Without a search term, only PUBLIC groups are
                    returned and PRIVATE groups are never recommended.

                    When a search term is provided:
                    - PUBLIC groups support partial,
                      case-insensitive name matching.
                    - PRIVATE groups are returned only when their full
                      name exactly matches the search term,
                      case-insensitively.

                    This allows users to find a known PRIVATE group
                    without exposing it through normal discovery.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Study groups retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    ref = "#/components/responses/Unauthorized"
            )
    })
    @GetMapping
    public Page<GroupResponse> getGroups(
            @RequestParam(defaultValue = "")
            String search,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size
    ) {
        Pageable pageable =
                PageRequest.of(
                        page,
                        size,
                        Sort.by(
                                Sort.Direction.DESC,
                                "createdAt"
                        )
                );

        return studyGroupService.getGroups(
                search,
                pageable
        );
    }

    @Operation(
            summary = "Get my study groups",
            description = """
                    Returns all groups in which the authenticated
                    user currently has an OWNER or MEMBER membership.

                    Both PUBLIC and PRIVATE groups are included.
                    """
    )
    @GetMapping("/mine")
    public Page<GroupResponse> getMyGroups(
            @Parameter(hidden = true)
            @AuthenticationPrincipal Jwt jwt,

            @RequestParam(defaultValue = "")
            String search,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size
    ) {
        UUID currentUserId =
                UUID.fromString(
                        jwt.getSubject()
                );

        Pageable pageable =
                PageRequest.of(
                        page,
                        size,
                        Sort.by(
                                Sort.Direction.DESC,
                                "joinedAt"
                        )
                );

        return studyGroupService.getMyGroups(
                currentUserId,
                search,
                pageable
        );
    }

    @Operation(
            summary = "Get study group details",
            description = """
                    Returns basic study-group metadata.

                    PUBLIC group metadata is available to any
                    authenticated user.

                    PRIVATE group metadata may also be viewed by an
                    authenticated user who knows the group, allowing
                    them to request access.

                    Private member content such as the member list,
                    group chat and study room remains protected.
                    """
    )
    @GetMapping("/{groupId}")
    public GroupResponse getGroupById(
            @Parameter(hidden = true)
            @AuthenticationPrincipal Jwt jwt,

            @PathVariable UUID groupId
    ) {
        UUID currentUserId =
                UUID.fromString(
                        jwt.getSubject()
                );

        return studyGroupService.getGroupById(
                groupId,
                currentUserId
        );
    }

    @Operation(
            summary = "Delete a study group",
            description = """
                    Deletes a study group.

                    Only the group owner may perform this operation.
                    """
    )
    @DeleteMapping("/{groupId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteGroup(
            @Parameter(hidden = true)
            @AuthenticationPrincipal Jwt jwt,

            @PathVariable UUID groupId
    ) {
        UUID currentUserId =
                UUID.fromString(
                        jwt.getSubject()
                );

        studyGroupService.deleteGroup(
                groupId,
                currentUserId
        );
    }

    @Operation(
            summary = "Join or request to join a study group",
            description = """
                    PUBLIC:
                    the authenticated user becomes a MEMBER
                    immediately.

                    PRIVATE:
                    a PENDING join request is created or reopened
                    and must be approved by the group owner.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Join operation accepted successfully"
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

            @PathVariable UUID groupId
    ) {
        UUID currentUserId =
                UUID.fromString(
                        jwt.getSubject()
                );

        studyGroupService.joinGroup(
                groupId,
                currentUserId
        );
    }

    @Operation(
            summary = "Get my join request",
            description = """
                    Returns the authenticated user's existing join
                    request for the specified group.

                    Returns 204 when no request exists.
                    """
    )
    @GetMapping("/{groupId}/join-request/me")
    public ResponseEntity<GroupJoinRequestResponse>
    getMyJoinRequest(
            @Parameter(hidden = true)
            @AuthenticationPrincipal Jwt jwt,

            @PathVariable UUID groupId
    ) {
        UUID currentUserId =
                UUID.fromString(
                        jwt.getSubject()
                );

        return studyGroupService
                .getMyJoinRequest(
                        groupId,
                        currentUserId
                )
                .map(ResponseEntity::ok)
                .orElseGet(
                        () ->
                                ResponseEntity
                                        .noContent()
                                        .build()
                );
    }

    @Operation(
            summary = "Get pending join requests",
            description = """
                    Returns all PENDING join requests for the group.

                    Only the group owner may access this endpoint.
                    """
    )
    @GetMapping("/{groupId}/join-requests")
    public List<GroupJoinRequestResponse>
    getPendingJoinRequests(
            @Parameter(hidden = true)
            @AuthenticationPrincipal Jwt jwt,

            @PathVariable UUID groupId
    ) {
        UUID currentUserId =
                UUID.fromString(
                        jwt.getSubject()
                );

        return studyGroupService
                .getPendingJoinRequests(
                        groupId,
                        currentUserId
                );
    }

    @Operation(
            summary = "Approve a join request",
            description = """
                    Approves a PENDING private-group join request.

                    A MEMBER membership is created for the requester.

                    Only the group owner may approve requests.
                    """
    )
    @PostMapping(
            "/{groupId}/join-requests/{requestId}/approve"
    )
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void approveJoinRequest(
            @Parameter(hidden = true)
            @AuthenticationPrincipal Jwt jwt,

            @PathVariable UUID groupId,

            @PathVariable UUID requestId
    ) {
        UUID currentUserId =
                UUID.fromString(
                        jwt.getSubject()
                );

        studyGroupService.approveJoinRequest(
                groupId,
                requestId,
                currentUserId
        );
    }

    @Operation(
            summary = "Reject a join request",
            description = """
                    Rejects a PENDING private-group join request.

                    Only the group owner may reject requests.
                    """
    )
    @PostMapping(
            "/{groupId}/join-requests/{requestId}/reject"
    )
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void rejectJoinRequest(
            @Parameter(hidden = true)
            @AuthenticationPrincipal Jwt jwt,

            @PathVariable UUID groupId,

            @PathVariable UUID requestId
    ) {
        UUID currentUserId =
                UUID.fromString(
                        jwt.getSubject()
                );

        studyGroupService.rejectJoinRequest(
                groupId,
                requestId,
                currentUserId
        );
    }

    @Operation(
            summary = "Leave a study group",
            description = """
                    Removes the authenticated user's MEMBER
                    membership from the group.

                    The OWNER cannot leave their own group.
                    """
    )
    @DeleteMapping("/{groupId}/members/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void leaveGroup(
            @Parameter(hidden = true)
            @AuthenticationPrincipal Jwt jwt,

            @PathVariable UUID groupId
    ) {
        UUID currentUserId =
                UUID.fromString(
                        jwt.getSubject()
                );

        studyGroupService.leaveGroup(
                groupId,
                currentUserId
        );
    }

    @Operation(
            summary = "Get study group members",
            description = """
                    Returns the group's member list.

                    PUBLIC group members may be viewed by authenticated
                    users.

                    For PRIVATE groups, only an existing OWNER or
                    MEMBER may access the member list.
                    """
    )
    @GetMapping("/{groupId}/members")
    public List<GroupMemberResponse> getMembers(
            @Parameter(hidden = true)
            @AuthenticationPrincipal Jwt jwt,

            @PathVariable UUID groupId
    ) {
        UUID currentUserId =
                UUID.fromString(
                        jwt.getSubject()
                );

        return studyGroupService.getMembers(
                groupId,
                currentUserId
        );
    }
}