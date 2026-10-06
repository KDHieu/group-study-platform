package com.grouplearning.backend.repository;

import com.grouplearning.backend.entity.CallRoom;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CallRoomRepository
        extends JpaRepository<CallRoom, UUID> {

    @EntityGraph(
            attributePaths = {
                    "group",
                    "createdBy"
            }
    )
    List<CallRoom> findByGroup_IdOrderByCreatedAtAsc(
            UUID groupId
    );

    @EntityGraph(
            attributePaths = {
                    "group",
                    "createdBy"
            }
    )
    Optional<CallRoom> findByIdAndGroup_Id(
            UUID callRoomId,
            UUID groupId
    );
}