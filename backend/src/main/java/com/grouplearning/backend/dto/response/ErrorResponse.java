package com.grouplearning.backend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.Map;

@Schema(description = "Standard error response returned by the API")
public record ErrorResponse(

        @Schema(
                description = "Time when the error occurred",
                example = "2026-10-01T03:30:00Z"
        )
        Instant timestamp,

        @Schema(
                description = "HTTP status code",
                example = "400"
        )
        int status,

        @Schema(
                description = "HTTP error name",
                example = "Bad Request"
        )
        String error,

        @Schema(
                description = "Human-readable error message",
                example = "Validation failed"
        )
        String message,

        @Schema(
                description = "Request path that caused the error",
                example = "/api/auth/register"
        )
        String path,

        @Schema(
                description = """
                        Field-specific validation errors.
                        The map key is the field name and the value is the validation message.
                        This value may be null or empty for non-validation errors.
                        """,
                example = """
                        {
                          "email": "must be a well-formed email address",
                          "password": "size must be between 8 and 100"
                        }
                        """
        )
        Map<String, String> fieldErrors

) {
}