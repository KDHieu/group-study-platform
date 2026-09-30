package com.grouplearning.backend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response returned after successful authentication")
public record LoginResponse(

        @Schema(
                description = "JWT access token used to authenticate subsequent requests",
                example = "eyJhbGciOiJSUzI1NiJ9..."
        )
        String accessToken,

        @Schema(
                description = "Authentication scheme used with the access token",
                example = "Bearer"
        )
        String tokenType,

        @Schema(
                description = "Access token lifetime in seconds",
                example = "3600"
        )
        long expiresIn,

        @Schema(
                description = "Information about the authenticated user"
        )
        UserResponse user

) {
}