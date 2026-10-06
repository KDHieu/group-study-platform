package com.grouplearning.backend.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "daily_challenge_attempts",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_daily_challenge_attempts_challenge_user",
                        columnNames = {
                                "challenge_id",
                                "user_id"
                        }
                )
        }
)
public class DailyChallengeAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "challenge_id",
            nullable = false
    )
    private DailyChallenge challenge;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private User user;

    @Column(
            name = "selected_option",
            nullable = false,
            length = 1
    )
    private String selectedOption;

    @Column(nullable = false)
    private boolean correct;

    @CreationTimestamp
    @Column(
            name = "completed_at",
            nullable = false,
            updatable = false
    )
    private Instant completedAt;

    protected DailyChallengeAttempt() {
    }

    public DailyChallengeAttempt(
            DailyChallenge challenge,
            User user,
            String selectedOption,
            boolean correct
    ) {
        this.challenge = challenge;
        this.user = user;
        this.selectedOption = selectedOption;
        this.correct = correct;
    }

    public UUID getId() {
        return id;
    }

    public DailyChallenge getChallenge() {
        return challenge;
    }

    public User getUser() {
        return user;
    }

    public String getSelectedOption() {
        return selectedOption;
    }

    public boolean isCorrect() {
        return correct;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }
}