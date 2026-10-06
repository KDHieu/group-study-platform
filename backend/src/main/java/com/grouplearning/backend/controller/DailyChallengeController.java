package com.grouplearning.backend.controller;

import com.grouplearning.backend.dto.request.SubmitDailyChallengeRequest;
import com.grouplearning.backend.dto.response.ChallengeHistoryResponse;
import com.grouplearning.backend.dto.response.ChallengeStreakResponse;
import com.grouplearning.backend.dto.response.TodayChallengeResponse;
import com.grouplearning.backend.service.DailyChallengeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/challenges")
@Tag(
        name = "Daily Challenges",
        description = "Daily learning challenge, history and streak APIs"
)
@SecurityRequirement(name = "bearerAuth")
public class DailyChallengeController {

    private final DailyChallengeService
            dailyChallengeService;

    public DailyChallengeController(
            DailyChallengeService dailyChallengeService
    ) {
        this.dailyChallengeService =
                dailyChallengeService;
    }

    @GetMapping("/today")
    @Operation(
            summary = "Get today's daily challenge"
    )
    public TodayChallengeResponse getTodayChallenge(
            @Parameter(hidden = true)
            @AuthenticationPrincipal
            Jwt jwt
    ) {
        return dailyChallengeService
                .getTodayChallenge(
                        extractUserId(jwt)
                );
    }

    @PostMapping("/today/submit")
    @Operation(
            summary = "Submit today's daily challenge"
    )
    public TodayChallengeResponse submitTodayChallenge(
            @Parameter(hidden = true)
            @AuthenticationPrincipal
            Jwt jwt,

            @Valid
            @RequestBody
            SubmitDailyChallengeRequest request
    ) {
        return dailyChallengeService
                .submitTodayChallenge(
                        extractUserId(jwt),
                        request
                );
    }

    @GetMapping("/history")
    @Operation(
            summary = "Get daily challenge history"
    )
    public Page<ChallengeHistoryResponse> getHistory(
            @Parameter(hidden = true)
            @AuthenticationPrincipal
            Jwt jwt,

            @PageableDefault(
                    size = 20
            )
            Pageable pageable
    ) {
        return dailyChallengeService
                .getHistory(
                        extractUserId(jwt),
                        pageable
                );
    }

    @GetMapping("/streak")
    @Operation(
            summary = "Get daily challenge streak"
    )
    public ChallengeStreakResponse getStreak(
            @Parameter(hidden = true)
            @AuthenticationPrincipal
            Jwt jwt
    ) {
        return dailyChallengeService
                .getStreak(
                        extractUserId(jwt)
                );
    }

    private UUID extractUserId(
            Jwt jwt
    ) {
        return UUID.fromString(
                jwt.getSubject()
        );
    }
}