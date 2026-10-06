package com.grouplearning.backend.dto.response;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ChallengeHistoryResponse(
        UUID challengeId,
        LocalDate challengeDate,
        String question,

        String selectedOption,
        String correctOption,

        boolean correct,

        String explanation,
        Instant completedAt
) {
}