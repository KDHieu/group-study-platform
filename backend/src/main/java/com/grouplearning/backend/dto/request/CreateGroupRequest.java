package com.grouplearning.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateGroupRequest(

        @NotBlank(message = "Group name is required")
        @Size(
                min = 3,
                max = 100,
                message = "Group name must be between 3 and 100 characters"
        )
        String name,

        @Size(
                max = 1000,
                message = "Description must not exceed 1000 characters"
        )
        String description
) {
}