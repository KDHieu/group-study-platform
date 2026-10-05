package com.grouplearning.backend.repository;

import com.grouplearning.backend.entity.GroupJoinRequest;
import com.grouplearning.backend.entity.GroupJoinRequestStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GroupJoinRequestRepository
        extends JpaRepository<GroupJoinRequest, UUID> {

    @EntityGraph(
            attributePaths = {
                    "requester",
                    "group",
                    "group.owner"
            }
    )
    Optional<GroupJoinRequest>
    findByGroup_IdAndRequester_Id(
            UUID groupId,
            UUID requesterId
    );

    @EntityGraph(
            attributePaths = {
                    "requester"
            }
    )
    List<GroupJoinRequest>
    findByGroup_IdAndStatusOrderByCreatedAtAsc(
            UUID groupId,
            GroupJoinRequestStatus status
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(
            attributePaths = {
                    "requester",
                    "group",
                    "group.owner"
            }
    )
    Optional<GroupJoinRequest>
    findByIdAndGroup_Id(
            UUID requestId,
            UUID groupId
    );
}