CREATE TABLE call_rooms
(
    id UUID PRIMARY KEY,

    group_id UUID NOT NULL,

    name VARCHAR(100) NOT NULL,

    created_by UUID NOT NULL,

    created_at TIMESTAMP WITH TIME ZONE
        NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_call_rooms_group
        FOREIGN KEY (group_id)
            REFERENCES study_groups (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_call_rooms_created_by
        FOREIGN KEY (created_by)
            REFERENCES users (id)
            ON DELETE CASCADE
);

CREATE INDEX idx_call_rooms_group_created_at
    ON call_rooms (
                   group_id,
                   created_at
        );

CREATE INDEX idx_call_rooms_created_by
    ON call_rooms (created_by);