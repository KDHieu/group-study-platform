package com.grouplearning.backend.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(
        name = "daily_challenges",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_daily_challenges_date",
                        columnNames = "challenge_date"
                )
        }
)
public class DailyChallenge {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(
            name = "challenge_date",
            nullable = false
    )
    private LocalDate challengeDate;

    @Column(
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String question;

    @Column(
            name = "option_a",
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String optionA;

    @Column(
            name = "option_b",
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String optionB;

    @Column(
            name = "option_c",
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String optionC;

    @Column(
            name = "option_d",
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String optionD;

    @Column(
            name = "correct_option",
            nullable = false,
            length = 1
    )
    private String correctOption;

    @Column(
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String explanation;

    @CreationTimestamp
    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    protected DailyChallenge() {
    }

    public DailyChallenge(
            LocalDate challengeDate,
            String question,
            String optionA,
            String optionB,
            String optionC,
            String optionD,
            String correctOption,
            String explanation
    ) {
        this.challengeDate = challengeDate;
        this.question = question;
        this.optionA = optionA;
        this.optionB = optionB;
        this.optionC = optionC;
        this.optionD = optionD;
        this.correctOption = correctOption;
        this.explanation = explanation;
    }

    public UUID getId() {
        return id;
    }

    public LocalDate getChallengeDate() {
        return challengeDate;
    }

    public String getQuestion() {
        return question;
    }

    public String getOptionA() {
        return optionA;
    }

    public String getOptionB() {
        return optionB;
    }

    public String getOptionC() {
        return optionC;
    }

    public String getOptionD() {
        return optionD;
    }

    public String getCorrectOption() {
        return correctOption;
    }

    public String getExplanation() {
        return explanation;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}