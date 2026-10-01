package com.grouplearning.backend.repository;

import com.grouplearning.backend.entity.ChatMessage;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ChatMessageRepository
        extends JpaRepository<ChatMessage, UUID> {

    @EntityGraph(
            attributePaths = {
                    "sender"
            }
    )
    Page<ChatMessage> findByGroup_Id(
            UUID groupId,
            Pageable pageable
    );
}