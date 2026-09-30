package com.grouplearning.backend.config;

import com.grouplearning.backend.dto.response.ErrorResponse;

import io.swagger.v3.core.converter.ModelConverters;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Group Study Platform API",
                version = "v1",
                description = """
                        REST API for the Group Study Platform.

                        Current features include:
                        - Authentication
                        - Study groups
                        - Group membership
                        """
        )
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class OpenApiConfig {

        @Bean
        public OpenAPI customOpenAPI() {

                Schema<?> errorResponseSchema =
                        ModelConverters
                                .getInstance()
                                .read(ErrorResponse.class)
                                .get("ErrorResponse");

                Components components = new Components()
                        .addSchemas(
                                "ErrorResponse",
                                errorResponseSchema
                        )

                        .addResponses(
                                "BadRequest",
                                createErrorResponse(
                                        "Invalid request or validation failed"
                                )
                        )

                        .addResponses(
                                "Unauthorized",
                                createErrorResponse(
                                        "Authentication is required or credentials are invalid"
                                )
                        )

                        .addResponses(
                                "Forbidden",
                                createErrorResponse(
                                        "The authenticated user does not have permission to perform this operation"
                                )
                        )

                        .addResponses(
                                "NotFound",
                                createErrorResponse(
                                        "The requested resource was not found"
                                )
                        )

                        .addResponses(
                                "Conflict",
                                createErrorResponse(
                                        "The request conflicts with the current state of the resource"
                                )
                        )

                        .addResponses(
                                "InternalServerError",
                                createErrorResponse(
                                        "An unexpected server error occurred"
                                )
                        );

                return new OpenAPI()
                        .components(components);
        }

        private ApiResponse createErrorResponse(
                String description
        ) {

                Schema<?> schema = new Schema<>()
                        .$ref("#/components/schemas/ErrorResponse");

                MediaType mediaType = new MediaType()
                        .schema(schema);

                Content content = new Content()
                        .addMediaType(
                                "application/json",
                                mediaType
                        );

                return new ApiResponse()
                        .description(description)
                        .content(content);
        }
}