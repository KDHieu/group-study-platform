package com.grouplearning.backend.dto.request;

import com.grouplearning.backend.entity.GroupVisibility;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(
        description = "Request payload for creating a study group"
)
public record CreateGroupRequest(

        @Schema(
                description = "Name of the study group",
                example = "Distributed Systems Study Group",
                minLength = 3,
                maxLength = 100
        )
        @NotBlank
        @Size(min = 3, max = 100)
        String name,

        @Schema(
                description = "Optional description of the study group",
                example = "A group for students learning distributed systems and microservices.",
                maxLength = 1000
        )
        @Size(max = 1000)
        String description,

        @Schema(
                description = "Visibility of the study group",
                example = "PUBLIC",
                defaultValue = "PUBLIC",
                allowableValues = {
                        "PUBLIC",
                        "PRIVATE"
                }
        )
        GroupVisibility visibility

) {

        public CreateGroupRequest {
                if (visibility == null) {
                        visibility = GroupVisibility.PUBLIC;
                }
        }

        /*
         * Backward-compatible constructor.
         *
         * Existing code and tests that create a group request
         * without specifying visibility continue to work.
         */
        public CreateGroupRequest(
                String name,
                String description
        ) {
                this(
                        name,
                        description,
                        GroupVisibility.PUBLIC
                );
        }
}