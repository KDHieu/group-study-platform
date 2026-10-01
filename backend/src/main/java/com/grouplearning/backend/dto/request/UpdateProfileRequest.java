package com.grouplearning.backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request payload for updating the current user's profile")
public record UpdateProfileRequest(

        @Schema(
                description = "Display name shown to other users",
                example = "Kiều Đăng Hiếu",
                minLength = 1,
                maxLength = 100
        )
        @NotBlank
        @Size(max = 100)
        String displayName,

        @Schema(
                description = "Short biography shown on the user's profile",
                example = "IT student interested in Software Engineering and AI.",
                maxLength = 500
        )
        @Size(max = 500)
        String bio

) {
}