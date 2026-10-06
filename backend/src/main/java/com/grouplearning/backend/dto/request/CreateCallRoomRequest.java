package com.grouplearning.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCallRoomRequest(

        @NotBlank
        @Size(
                min = 1,
                max = 100
        )
        String name

) {
}