package com.grouplearning.backend.repository;

import com.grouplearning.backend.entity.DailyChallengeAttempt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DailyChallengeAttemptRepository
        extends JpaRepository<DailyChallengeAttempt, UUID> {

    @EntityGraph(
            attributePaths = {
                    "challenge",
                    "user"
            }
    )
    Optional<DailyChallengeAttempt>
    findByChallenge_IdAndUser_Id(
            UUID challengeId,
            UUID userId
    );

    @EntityGraph(
            attributePaths = {
                    "challenge"
            }
    )
    Page<DailyChallengeAttempt>
    findByUser_IdOrderByCompletedAtDesc(
            UUID userId,
            Pageable pageable
    );

    @Query("""
            SELECT a.challenge.challengeDate
            FROM DailyChallengeAttempt a
            WHERE a.user.id = :userId
            ORDER BY a.challenge.challengeDate ASC
            """)
    List<LocalDate> findCompletedDates(
            @Param("userId")
            UUID userId
    );
}