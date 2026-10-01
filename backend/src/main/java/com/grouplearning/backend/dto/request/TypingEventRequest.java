package com.grouplearning.backend.dto.request;

import jakarta.validation.constraints.NotNull;

public record TypingEventRequest(

        @NotNull
        Boolean typing

) {
}