ALTER TABLE chat_messages
    ADD COLUMN duration_ms INTEGER;

ALTER TABLE chat_messages
    ADD CONSTRAINT chk_chat_messages_duration_ms
        CHECK (
            duration_ms IS NULL
                OR duration_ms >= 0
            );