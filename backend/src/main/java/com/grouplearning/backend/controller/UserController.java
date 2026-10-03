package com.grouplearning.backend.controller;

import com.grouplearning.backend.dto.request.UpdateProfileRequest;
import com.grouplearning.backend.dto.response.UserProfileResponse;
import com.grouplearning.backend.dto.response.UserSearchResponse;
import com.grouplearning.backend.service.ObjectStorageService;
import com.grouplearning.backend.service.UserService;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@Tag(
        name = "Users",
        description = "User profile, avatar and user search APIs"
)
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    public UserController(
            UserService userService
    ) {
        this.userService =
                userService;
    }

    // =========================================================
    // Profile
    // =========================================================

    @GetMapping("/me/profile")
    @Operation(
            summary = "Get current user's profile",
            description = "Returns profile information of the authenticated user"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Profile retrieved successfully"
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
    public ResponseEntity<UserProfileResponse> getMyProfile(
            @Parameter(hidden = true)
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId =
                UUID.fromString(
                        jwt.getSubject()
                );

        return ResponseEntity.ok(
                userService.getProfile(
                        userId
                )
        );
    }

    @PatchMapping("/me/profile")
    @Operation(
            summary = "Update current user's profile",
            description = "Updates display name and biography of the authenticated user"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Profile updated successfully"
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
                    responseCode = "404",
                    ref = "#/components/responses/NotFound"
            )
    })
    public ResponseEntity<UserProfileResponse> updateMyProfile(
            @Parameter(hidden = true)
            @AuthenticationPrincipal Jwt jwt,

            @Valid
            @RequestBody
            UpdateProfileRequest request
    ) {
        UUID userId =
                UUID.fromString(
                        jwt.getSubject()
                );

        return ResponseEntity.ok(
                userService.updateProfile(
                        userId,
                        request
                )
        );
    }

    @GetMapping("/{userId}/profile")
    @Operation(
            summary = "Get user profile",
            description = "Returns public profile information of another user"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Profile retrieved successfully"
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
    public ResponseEntity<UserProfileResponse> getUserProfile(
            @Parameter(
                    description = "ID of the user",
                    required = true
            )
            @PathVariable UUID userId
    ) {
        return ResponseEntity.ok(
                userService.getProfile(
                        userId
                )
        );
    }

    // =========================================================
    // Avatar
    // =========================================================

    @PatchMapping(
            value = "/me/avatar",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @Operation(
            summary = "Update current user's avatar",
            description = "Uploads a new avatar for the authenticated user"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Avatar updated successfully"
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
                    responseCode = "404",
                    ref = "#/components/responses/NotFound"
            )
    })
    public ResponseEntity<UserProfileResponse> updateAvatar(
            @Parameter(hidden = true)
            @AuthenticationPrincipal Jwt jwt,

            @RequestPart("file")
            MultipartFile file
    ) {
        UUID userId =
                UUID.fromString(
                        jwt.getSubject()
                );

        return ResponseEntity.ok(
                userService.updateAvatar(
                        userId,
                        file
                )
        );
    }

    @GetMapping("/{userId}/avatar")
    @Operation(
            summary = "Get user avatar",
            description = "Returns the avatar image of a user"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Avatar retrieved successfully"
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
    public ResponseEntity<byte[]> getAvatar(
            @PathVariable UUID userId
    ) {
        ObjectStorageService.StoredObject avatar =
                userService.getAvatar(
                        userId
                );

        MediaType mediaType;

        try {
            mediaType =
                    MediaType.parseMediaType(
                            avatar.contentType()
                    );
        } catch (Exception exception) {
            mediaType =
                    MediaType.APPLICATION_OCTET_STREAM;
        }

        return ResponseEntity
                .ok()
                .contentType(
                        mediaType
                )
                .body(
                        avatar.content()
                );
    }

    // =========================================================
    // User search
    // =========================================================

    @GetMapping("/search")
    @Operation(
            summary = "Search users",
            description = "Searches users by username or display name"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Users retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    ref = "#/components/responses/Unauthorized"
            )
    })
    public ResponseEntity<Page<UserSearchResponse>> searchUsers(
            @Parameter(hidden = true)
            @AuthenticationPrincipal Jwt jwt,

            @RequestParam(defaultValue = "")
            String query,

            @Parameter(hidden = true)
            @PageableDefault(
                    size = 10,
                    sort = "username",
                    direction = Sort.Direction.ASC
            )
            Pageable pageable
    ) {
        UUID currentUserId =
                UUID.fromString(
                        jwt.getSubject()
                );

        return ResponseEntity.ok(
                userService.searchUsers(
                        currentUserId,
                        query,
                        pageable
                )
        );
    }
}