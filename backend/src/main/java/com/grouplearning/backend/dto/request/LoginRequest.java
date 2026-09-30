package com.grouplearning.backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request payload for user authentication")
public record LoginRequest(

        @Schema(
                description = "Email address of the registered user",
                example = "hieu@example.com"
        )
        @NotBlank
        @Email
        String email,

        @Schema(
                description = "User password",
                example = "12345678"
        )
        @NotBlank
        String password

) {
}