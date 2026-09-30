CREATE TABLE group_members (
                               id UUID PRIMARY KEY,
                               group_id UUID NOT NULL,
                               user_id UUID NOT NULL,
                               role VARCHAR(20) NOT NULL,
                               joined_at TIMESTAMP WITH TIME ZONE NOT NULL,

                               CONSTRAINT fk_group_members_group
                                   FOREIGN KEY (group_id)
                                       REFERENCES study_groups(id)
                                       ON DELETE CASCADE,

                               CONSTRAINT fk_group_members_user
                                   FOREIGN KEY (user_id)
                                       REFERENCES users(id)
                                       ON DELETE CASCADE,

                               CONSTRAINT uk_group_members_group_user
                                   UNIQUE (group_id, user_id)
);

CREATE INDEX idx_group_members_group_id
    ON group_members(group_id);

CREATE INDEX idx_group_members_user_id
    ON group_members(user_id);