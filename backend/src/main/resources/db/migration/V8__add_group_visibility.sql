ALTER TABLE study_groups
    ADD COLUMN visibility VARCHAR(20) NOT NULL DEFAULT 'PUBLIC';

ALTER TABLE study_groups
    ADD CONSTRAINT study_groups_visibility_check
        CHECK (visibility IN ('PUBLIC', 'PRIVATE'));