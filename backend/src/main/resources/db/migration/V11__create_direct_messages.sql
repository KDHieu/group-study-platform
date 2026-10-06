CREATE TABLE direct_messages
(
    id UUID PRIMARY KEY,

    sender_id UUID NOT NULL,

    receiver_id UUID NOT NULL,

    content TEXT NOT NULL,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_direct_messages_sender
        FOREIGN KEY (sender_id)
            REFERENCES users (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_direct_messages_receiver
        FOREIGN KEY (receiver_id)
            REFERENCES users (id)
            ON DELETE CASCADE,

    CONSTRAINT chk_direct_messages_not_self
        CHECK (sender_id <> receiver_id)
);

CREATE INDEX idx_direct_messages_sender_receiver_created_at
    ON direct_messages (
                        sender_id,
                        receiver_id,
                        created_at DESC
        );

CREATE INDEX idx_direct_messages_receiver_sender_created_at
    ON direct_messages (
                        receiver_id,
                        sender_id,
                        created_at DESC
        );