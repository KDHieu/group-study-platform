CREATE TABLE daily_challenges
(
    id UUID PRIMARY KEY,
    challenge_date DATE NOT NULL,
    question TEXT NOT NULL,
    option_a TEXT NOT NULL,
    option_b TEXT NOT NULL,
    option_c TEXT NOT NULL,
    option_d TEXT NOT NULL,
    correct_option VARCHAR(1) NOT NULL,
    explanation TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_daily_challenges_date
        UNIQUE (challenge_date),

    CONSTRAINT chk_daily_challenges_correct_option
        CHECK (correct_option IN ('A', 'B', 'C', 'D'))
);

CREATE TABLE daily_challenge_attempts
(
    id UUID PRIMARY KEY,
    challenge_id UUID NOT NULL,
    user_id UUID NOT NULL,
    selected_option VARCHAR(1) NOT NULL,
    correct BOOLEAN NOT NULL,
    completed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_daily_challenge_attempts_challenge
        FOREIGN KEY (challenge_id)
            REFERENCES daily_challenges (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_daily_challenge_attempts_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE CASCADE,

    CONSTRAINT uk_daily_challenge_attempts_challenge_user
        UNIQUE (challenge_id, user_id),

    CONSTRAINT chk_daily_challenge_attempts_selected_option
        CHECK (selected_option IN ('A', 'B', 'C', 'D'))
);

CREATE INDEX idx_daily_challenge_attempts_user_completed_at
    ON daily_challenge_attempts (user_id, completed_at DESC);

CREATE INDEX idx_daily_challenge_attempts_challenge
    ON daily_challenge_attempts (challenge_id);