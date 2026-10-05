CREATE TABLE group_join_requests
(
    id         UUID PRIMARY KEY,
    group_id   UUID        NOT NULL,
    user_id    UUID        NOT NULL,
    status     VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_group_join_requests_group
        FOREIGN KEY (group_id)
            REFERENCES study_groups (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_group_join_requests_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE CASCADE,

    CONSTRAINT uk_group_join_requests_group_user
        UNIQUE (group_id, user_id),

    CONSTRAINT chk_group_join_requests_status
        CHECK (
            status IN (
                       'PENDING',
                       'APPROVED',
                       'REJECTED'
                )
            )
);

CREATE INDEX idx_group_join_requests_group_status
    ON group_join_requests (
                            group_id,
                            status
        );

CREATE INDEX idx_group_join_requests_user
    ON group_join_requests (
                            user_id
        );