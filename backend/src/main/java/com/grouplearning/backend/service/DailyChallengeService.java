package com.grouplearning.backend.service;

import com.grouplearning.backend.dto.request.SubmitDailyChallengeRequest;
import com.grouplearning.backend.dto.response.ChallengeHistoryResponse;
import com.grouplearning.backend.dto.response.ChallengeOptionResponse;
import com.grouplearning.backend.dto.response.ChallengeStreakResponse;
import com.grouplearning.backend.dto.response.TodayChallengeResponse;
import com.grouplearning.backend.entity.DailyChallenge;
import com.grouplearning.backend.entity.DailyChallengeAttempt;
import com.grouplearning.backend.entity.User;
import com.grouplearning.backend.exception.ConflictException;
import com.grouplearning.backend.exception.NotFoundException;
import com.grouplearning.backend.repository.DailyChallengeAttemptRepository;
import com.grouplearning.backend.repository.DailyChallengeRepository;
import com.grouplearning.backend.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class DailyChallengeService {

    private static final ZoneId APPLICATION_ZONE =
            ZoneId.of(
                    "Asia/Ho_Chi_Minh"
            );

    /*
     * Temporary Phase 1 challenge catalog.
     *
     * A challenge is selected deterministically by date and then
     * persisted in daily_challenges.
     *
     * This keeps the feature usable every day without requiring
     * an administrator or AI challenge generator yet.
     */
    private static final List<ChallengeSeed>
            CHALLENGE_CATALOG =
            List.of(
                    new ChallengeSeed(
                            "Which data structure follows the LIFO principle?",
                            "Queue",
                            "Stack",
                            "Linked list",
                            "Graph",
                            "B",
                            "A stack follows Last In, First Out (LIFO)."
                    ),

                    new ChallengeSeed(
                            "What is the average time complexity of lookup in a well-designed hash table?",
                            "O(1)",
                            "O(log n)",
                            "O(n)",
                            "O(n log n)",
                            "A",
                            "Hash tables provide average-case constant-time lookup when hashing and bucket distribution are effective."
                    ),

                    new ChallengeSeed(
                            "Which HTTP method is normally used to retrieve a resource without modifying it?",
                            "POST",
                            "PATCH",
                            "GET",
                            "DELETE",
                            "C",
                            "GET is intended for retrieving representations of resources and should be safe."
                    ),

                    new ChallengeSeed(
                            "Which SQL clause is used to filter rows before grouping?",
                            "HAVING",
                            "WHERE",
                            "ORDER BY",
                            "LIMIT",
                            "B",
                            "WHERE filters individual rows before GROUP BY is applied."
                    ),

                    new ChallengeSeed(
                            "What does a database transaction primarily provide?",
                            "Only faster queries",
                            "Automatic API documentation",
                            "A logical unit of work with consistency guarantees",
                            "Frontend state management",
                            "C",
                            "A transaction groups operations into a logical unit of work and supports properties such as atomicity and consistency."
                    ),

                    new ChallengeSeed(
                            "Which traversal uses a queue to explore a graph level by level?",
                            "Depth-first search",
                            "Breadth-first search",
                            "Binary search",
                            "Dijkstra without a priority queue",
                            "B",
                            "Breadth-first search uses a queue and explores vertices by increasing distance in unweighted graphs."
                    ),

                    new ChallengeSeed(
                            "Which principle recommends that a class should have one primary reason to change?",
                            "Open/Closed Principle",
                            "Dependency Inversion Principle",
                            "Single Responsibility Principle",
                            "Interface Segregation Principle",
                            "C",
                            "The Single Responsibility Principle states that a module or class should have one primary responsibility or reason to change."
                    ),

                    new ChallengeSeed(
                            "Which Git command creates a new branch and switches to it in one operation?",
                            "git add",
                            "git switch -c",
                            "git fetch",
                            "git merge",
                            "B",
                            "git switch -c <branch> creates a new branch and switches the working tree to it."
                    ),

                    new ChallengeSeed(
                            "What is the main purpose of an API Gateway in a distributed system?",
                            "Compile Java source code",
                            "Provide a single entry point and route requests to backend services",
                            "Replace the database",
                            "Render React components",
                            "B",
                            "An API Gateway commonly acts as the external entry point and routes requests to appropriate backend services."
                    ),

                    new ChallengeSeed(
                            "Which testing level focuses on individual classes or functions in isolation?",
                            "Unit testing",
                            "System testing",
                            "Acceptance testing",
                            "Load testing",
                            "A",
                            "Unit testing verifies small isolated units such as methods, functions, or classes."
                    )
            );

    private final DailyChallengeRepository
            dailyChallengeRepository;

    private final DailyChallengeAttemptRepository
            attemptRepository;

    private final UserRepository
            userRepository;

    public DailyChallengeService(
            DailyChallengeRepository dailyChallengeRepository,
            DailyChallengeAttemptRepository attemptRepository,
            UserRepository userRepository
    ) {
        this.dailyChallengeRepository =
                dailyChallengeRepository;

        this.attemptRepository =
                attemptRepository;

        this.userRepository =
                userRepository;
    }

    @Transactional
    public TodayChallengeResponse getTodayChallenge(
            UUID userId
    ) {
        ensureUserExists(
                userId
        );

        LocalDate today =
                today();

        DailyChallenge challenge =
                getOrCreateChallenge(
                        today
                );

        return attemptRepository
                .findByChallenge_IdAndUser_Id(
                        challenge.getId(),
                        userId
                )
                .map(
                        attempt ->
                                toTodayResponse(
                                        challenge,
                                        attempt
                                )
                )
                .orElseGet(
                        () ->
                                toTodayResponse(
                                        challenge,
                                        null
                                )
                );
    }

    @Transactional
    public TodayChallengeResponse submitTodayChallenge(
            UUID userId,
            SubmitDailyChallengeRequest request
    ) {
        User user =
                findUser(
                        userId
                );

        DailyChallenge challenge =
                getOrCreateChallenge(
                        today()
                );

        if (attemptRepository
                .findByChallenge_IdAndUser_Id(
                        challenge.getId(),
                        userId
                )
                .isPresent()) {

            throw new ConflictException(
                    "Daily challenge has already been completed"
            );
        }

        String selectedOption =
                normalizeOption(
                        request.selectedOption()
                );

        boolean correct =
                selectedOption.equals(
                        challenge
                                .getCorrectOption()
                );

        DailyChallengeAttempt attempt =
                new DailyChallengeAttempt(
                        challenge,
                        user,
                        selectedOption,
                        correct
                );

        try {
            DailyChallengeAttempt saved =
                    attemptRepository
                            .saveAndFlush(
                                    attempt
                            );

            return toTodayResponse(
                    challenge,
                    saved
            );

        } catch (DataIntegrityViolationException exception) {
            throw new ConflictException(
                    "Daily challenge has already been completed"
            );
        }
    }

    @Transactional(readOnly = true)
    public Page<ChallengeHistoryResponse>
    getHistory(
            UUID userId,
            Pageable pageable
    ) {
        ensureUserExists(
                userId
        );

        return attemptRepository
                .findByUser_IdOrderByCompletedAtDesc(
                        userId,
                        pageable
                )
                .map(
                        this::toHistoryResponse
                );
    }

    @Transactional(readOnly = true)
    public ChallengeStreakResponse getStreak(
            UUID userId
    ) {
        ensureUserExists(
                userId
        );

        List<LocalDate> dates =
                attemptRepository
                        .findCompletedDates(
                                userId
                        )
                        .stream()
                        .distinct()
                        .toList();

        return new ChallengeStreakResponse(
                calculateCurrentStreak(
                        dates
                ),
                calculateLongestStreak(
                        dates
                ),
                dates.size()
        );
    }

    private DailyChallenge getOrCreateChallenge(
            LocalDate challengeDate
    ) {
        return dailyChallengeRepository
                .findByChallengeDate(
                        challengeDate
                )
                .orElseGet(
                        () ->
                                createChallenge(
                                        challengeDate
                                )
                );
    }

    private DailyChallenge createChallenge(
            LocalDate challengeDate
    ) {
        long indexValue =
                Math.floorMod(
                        challengeDate
                                .toEpochDay(),
                        CHALLENGE_CATALOG
                                .size()
                );

        ChallengeSeed seed =
                CHALLENGE_CATALOG.get(
                        (int) indexValue
                );

        DailyChallenge challenge =
                new DailyChallenge(
                        challengeDate,
                        seed.question(),
                        seed.optionA(),
                        seed.optionB(),
                        seed.optionC(),
                        seed.optionD(),
                        seed.correctOption(),
                        seed.explanation()
                );

        try {
            return dailyChallengeRepository
                    .saveAndFlush(
                            challenge
                    );

        } catch (DataIntegrityViolationException exception) {
            return dailyChallengeRepository
                    .findByChallengeDate(
                            challengeDate
                    )
                    .orElseThrow(
                            () ->
                                    exception
                    );
        }
    }

    private TodayChallengeResponse toTodayResponse(
            DailyChallenge challenge,
            DailyChallengeAttempt attempt
    ) {
        boolean completed =
                attempt != null;

        return new TodayChallengeResponse(
                challenge.getId(),
                challenge.getChallengeDate(),
                challenge.getQuestion(),

                List.of(
                        new ChallengeOptionResponse(
                                "A",
                                challenge.getOptionA()
                        ),
                        new ChallengeOptionResponse(
                                "B",
                                challenge.getOptionB()
                        ),
                        new ChallengeOptionResponse(
                                "C",
                                challenge.getOptionC()
                        ),
                        new ChallengeOptionResponse(
                                "D",
                                challenge.getOptionD()
                        )
                ),

                completed,

                completed
                        ? attempt.getSelectedOption()
                        : null,

                completed
                        ? attempt.isCorrect()
                        : null,

                completed
                        ? challenge.getCorrectOption()
                        : null,

                completed
                        ? challenge.getExplanation()
                        : null,

                completed
                        ? attempt.getCompletedAt()
                        : null
        );
    }

    private ChallengeHistoryResponse toHistoryResponse(
            DailyChallengeAttempt attempt
    ) {
        DailyChallenge challenge =
                attempt.getChallenge();

        return new ChallengeHistoryResponse(
                challenge.getId(),
                challenge.getChallengeDate(),
                challenge.getQuestion(),
                attempt.getSelectedOption(),
                challenge.getCorrectOption(),
                attempt.isCorrect(),
                challenge.getExplanation(),
                attempt.getCompletedAt()
        );
    }

    private int calculateCurrentStreak(
            List<LocalDate> completedDates
    ) {
        if (completedDates.isEmpty()) {
            return 0;
        }

        List<LocalDate> dates =
                new ArrayList<>(
                        completedDates
                );

        dates.sort(
                LocalDate::compareTo
        );

        LocalDate latest =
                dates.get(
                        dates.size() - 1
                );

        LocalDate today =
                today();

        LocalDate yesterday =
                today.minusDays(
                        1
                );

        if (
                !latest.equals(today) &&
                        !latest.equals(yesterday)
        ) {
            return 0;
        }

        int streak = 1;

        LocalDate expected =
                latest.minusDays(
                        1
                );

        for (
                int index =
                dates.size() - 2;
                index >= 0;
                index--
        ) {
            LocalDate date =
                    dates.get(
                            index
                    );

            if (date.equals(expected)) {
                streak++;

                expected =
                        expected.minusDays(
                                1
                        );

                continue;
            }

            if (
                    date.isBefore(
                            expected
                    )
            ) {
                break;
            }
        }

        return streak;
    }

    private int calculateLongestStreak(
            List<LocalDate> completedDates
    ) {
        if (completedDates.isEmpty()) {
            return 0;
        }

        List<LocalDate> dates =
                new ArrayList<>(
                        completedDates
                );

        dates.sort(
                LocalDate::compareTo
        );

        int longest = 1;
        int current = 1;

        for (
                int index = 1;
                index < dates.size();
                index++
        ) {
            LocalDate previous =
                    dates.get(
                            index - 1
                    );

            LocalDate currentDate =
                    dates.get(
                            index
                    );

            if (
                    currentDate.equals(
                            previous.plusDays(
                                    1
                            )
                    )
            ) {
                current++;

                longest =
                        Math.max(
                                longest,
                                current
                        );
            } else {
                current = 1;
            }
        }

        return longest;
    }

    private String normalizeOption(
            String value
    ) {
        if (value == null) {
            throw new IllegalArgumentException(
                    "Selected option is required"
            );
        }

        String normalized =
                value
                        .trim()
                        .toUpperCase(
                                Locale.ROOT
                        );

        if (
                !List.of(
                        "A",
                        "B",
                        "C",
                        "D"
                ).contains(
                        normalized
                )
        ) {
            throw new IllegalArgumentException(
                    "Selected option must be A, B, C or D"
            );
        }

        return normalized;
    }

    private LocalDate today() {
        return LocalDate.now(
                APPLICATION_ZONE
        );
    }

    private void ensureUserExists(
            UUID userId
    ) {
        if (
                !userRepository
                        .existsById(
                                userId
                        )
        ) {
            throw new NotFoundException(
                    "User not found"
            );
        }
    }

    private User findUser(
            UUID userId
    ) {
        return userRepository
                .findById(
                        userId
                )
                .orElseThrow(
                        () ->
                                new NotFoundException(
                                        "User not found"
                                )
                );
    }

    private record ChallengeSeed(
            String question,
            String optionA,
            String optionB,
            String optionC,
            String optionD,
            String correctOption,
            String explanation
    ) {
    }
}