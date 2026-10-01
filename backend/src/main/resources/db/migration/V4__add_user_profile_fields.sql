ALTER TABLE users
    ADD COLUMN display_name VARCHAR(100),
    ADD COLUMN bio VARCHAR(500),
    ADD COLUMN avatar_url VARCHAR(1000);

UPDATE users
SET display_name = username
WHERE display_name IS NULL;

ALTER TABLE users
    ALTER COLUMN display_name SET NOT NULL;