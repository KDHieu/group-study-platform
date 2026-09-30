CREATE TABLE study_groups (
                              id UUID PRIMARY KEY,
                              name VARCHAR(100) NOT NULL,
                              description VARCHAR(1000),
                              owner_id UUID NOT NULL,
                              created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                              updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

                              CONSTRAINT fk_study_groups_owner
                                  FOREIGN KEY (owner_id)
                                      REFERENCES users(id)
                                      ON DELETE CASCADE
);

CREATE INDEX idx_study_groups_owner_id
    ON study_groups(owner_id);

CREATE INDEX idx_study_groups_name_lower
    ON study_groups(LOWER(name));