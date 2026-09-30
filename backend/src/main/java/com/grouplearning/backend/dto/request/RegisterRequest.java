package com.grouplearning.backend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request payload for registering a new user")
public record RegisterRequest(

        @Schema(
                description = "Unique username of the user",
                example = "hieu",
                minLength = 3,
                maxLength = 50
        )
        @NotBlank
        @Size(min = 3, max = 50)
        String username,

        @Schema(
                description = "Email address used for authentication",
                example = "hieu@example.com"
        )
        @NotBlank
        @Email
        String email,

        @Schema(
                description = "Password of the new account",
                example = "12345678",
                minLength = 8,
                maxLength = 100
        )
        @NotBlank
        @Size(min = 8, max = 100)
        String password

) {
}