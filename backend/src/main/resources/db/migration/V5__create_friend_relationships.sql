CREATE TABLE friend_relationships (
                                      id UUID PRIMARY KEY,

                                      requester_id UUID NOT NULL,
                                      addressee_id UUID NOT NULL,

                                      status VARCHAR(20) NOT NULL,

                                      created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                      updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

                                      CONSTRAINT fk_friend_relationships_requester
                                          FOREIGN KEY (requester_id)
                                              REFERENCES users(id)
                                              ON DELETE CASCADE,

                                      CONSTRAINT fk_friend_relationships_addressee
                                          FOREIGN KEY (addressee_id)
                                              REFERENCES users(id)
                                              ON DELETE CASCADE,

                                      CONSTRAINT chk_friend_relationships_not_self
                                          CHECK (requester_id <> addressee_id),

                                      CONSTRAINT chk_friend_relationships_status
                                          CHECK (status IN ('PENDING', 'ACCEPTED'))
);

CREATE UNIQUE INDEX uk_friend_relationships_pair
    ON friend_relationships (
                             LEAST(requester_id, addressee_id),
                             GREATEST(requester_id, addressee_id)
        );

CREATE INDEX idx_friend_relationships_requester
    ON friend_relationships(requester_id);

CREATE INDEX idx_friend_relationships_addressee
    ON friend_relationships(addressee_id);

CREATE INDEX idx_friend_relationships_status
    ON friend_relationships(status);