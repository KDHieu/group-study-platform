package com.grouplearning.backend.dto.response;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record TodayChallengeResponse(
        UUID id,
        LocalDate challengeDate,
        String question,
        List<ChallengeOptionResponse> options,

        boolean completed,

        String selectedOption,
        Boolean correct,

        String correctOption,
        String explanation,

        Instant completedAt
) {
}