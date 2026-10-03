package com.grouplearning.backend.controller;

import com.grouplearning.backend.dto.request.CreateGroupRequest;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/groups")
@Tag(
        name = "Study Groups",
        description = "Study group management and membership APIs"
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

    // =========================================================
    // Group management
    // =========================================================

    @Operation(
            summary = "Create a study group",
            description = """
                    Creates a new study group.

                    The authenticated user automatically becomes the owner
                    and is added as an OWNER member of the group.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Study group created successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    ref = "#/components/responses/BadRequest"
            ),
            @ApiResponse(
                    responseCode = "401",
                    ref = "#/components/responses/Unauthorized"
            )
    })
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
            summary = "Get study groups",
            description = """
                    Returns a paginated list of study groups.

                    The optional search parameter filters groups by name.
                    Results are sorted by creation time in descending order.
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
            @Parameter(
                    description = "Optional text used to search study groups by name",
                    example = "distributed systems"
            )
            @RequestParam(defaultValue = "")
            String search,

            @Parameter(
                    description = "Zero-based page number",
                    example = "0"
            )
            @RequestParam(defaultValue = "0")
            int page,

            @Parameter(
                    description = "Number of study groups per page",
                    example = "10"
            )
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
            summary = "Get study group details",
            description = """
                    Returns detailed information about a study group
                    identified by its unique ID.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Study group retrieved successfully"
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
    @GetMapping("/{groupId}")
    public GroupResponse getGroupById(
            @Parameter(
                    description = "Unique identifier of the study group",
                    example = "550e8400-e29b-41d4-a716-446655440000"
            )
            @PathVariable UUID groupId
    ) {
        return studyGroupService.getGroupById(
                groupId
        );
    }

    @Operation(
            summary = "Delete a study group",
            description = """
                    Deletes an existing study group.

                    Only the owner of the group is allowed to perform this operation.

                    Associated group memberships are automatically removed
                    through database cascading.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Study group deleted successfully"
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
    @DeleteMapping("/{groupId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteGroup(
            @Parameter(hidden = true)
            @AuthenticationPrincipal Jwt jwt,

            @Parameter(
                    description = "Unique identifier of the study group",
                    example = "550e8400-e29b-41d4-a716-446655440000"
            )
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

    // =========================================================
    // Group membership
    // =========================================================

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
                UUID.fromString(
                        jwt.getSubject()
                );

        studyGroupService.joinGroup(
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
        return studyGroupService.getMembers(
                groupId
        );
    }
}