CREATE TABLE chat_messages (
                               id UUID PRIMARY KEY,

                               group_id UUID NOT NULL,
                               sender_id UUID NOT NULL,

                               type VARCHAR(20) NOT NULL,
                               content TEXT,
                               media_url VARCHAR(1000),

                               created_at TIMESTAMP WITH TIME ZONE NOT NULL,

                               CONSTRAINT fk_chat_messages_group
                                   FOREIGN KEY (group_id)
                                       REFERENCES study_groups(id)
                                       ON DELETE CASCADE,

                               CONSTRAINT fk_chat_messages_sender
                                   FOREIGN KEY (sender_id)
                                       REFERENCES users(id)
                                       ON DELETE CASCADE,

                               CONSTRAINT chk_chat_messages_type
                                   CHECK (
                                       type IN (
                                                'TEXT',
                                                'AUDIO',
                                                'IMAGE',
                                                'FILE'
                                           )
                                       )
);

CREATE INDEX idx_chat_messages_group_created_at
    ON chat_messages (
                      group_id,
                      created_at DESC
        );

CREATE INDEX idx_chat_messages_sender
    ON chat_messages(sender_id);