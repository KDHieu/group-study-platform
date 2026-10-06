package com.grouplearning.backend.dto.response;

public record ChallengeStreakResponse(
        int currentStreak,
        int longestStreak,
        int completedChallenges
) {
}