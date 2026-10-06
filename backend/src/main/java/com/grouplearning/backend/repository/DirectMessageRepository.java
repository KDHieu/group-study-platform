package com.grouplearning.backend.repository;

import com.grouplearning.backend.entity.DirectMessage;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface DirectMessageRepository
        extends JpaRepository<DirectMessage, UUID> {

    @Query(
            value = """
                    SELECT dm
                    FROM DirectMessage dm
                    WHERE
                        (
                            dm.sender.id = :userA
                            AND dm.receiver.id = :userB
                        )
                        OR
                        (
                            dm.sender.id = :userB
                            AND dm.receiver.id = :userA
                        )
                    ORDER BY dm.createdAt DESC
                    """,
            countQuery = """
                    SELECT COUNT(dm)
                    FROM DirectMessage dm
                    WHERE
                        (
                            dm.sender.id = :userA
                            AND dm.receiver.id = :userB
                        )
                        OR
                        (
                            dm.sender.id = :userB
                            AND dm.receiver.id = :userA
                        )
                    """
    )
    Page<DirectMessage> findConversation(
            @Param("userA")
            UUID userA,

            @Param("userB")
            UUID userB,

            Pageable pageable
    );

    @Query("""
            SELECT dm
            FROM DirectMessage dm
            WHERE
                (
                    dm.sender.id = :userA
                    AND dm.receiver.id = :userB
                )
                OR
                (
                    dm.sender.id = :userB
                    AND dm.receiver.id = :userA
                )
            ORDER BY dm.createdAt DESC
            """)
    List<DirectMessage> findLatestBetweenUsers(
            @Param("userA")
            UUID userA,

            @Param("userB")
            UUID userB,

            Pageable pageable
    );
}