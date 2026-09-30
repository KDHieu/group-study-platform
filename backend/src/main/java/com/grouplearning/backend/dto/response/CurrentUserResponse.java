package com.grouplearning.backend.dto.response;

import java.util.UUID;

public record CurrentUserResponse(
        UUID id,
        String username
) {
}