package com.grouplearning.backend.repository;

import com.grouplearning.backend.entity.GroupMember;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GroupMemberRepository
        extends JpaRepository<GroupMember, UUID> {

    boolean existsByGroup_IdAndUser_Id(
            UUID groupId,
            UUID userId
    );

    Optional<GroupMember> findByGroup_IdAndUser_Id(
            UUID groupId,
            UUID userId
    );

    @EntityGraph(attributePaths = "user")
    List<GroupMember> findByGroup_IdOrderByJoinedAtAsc(
            UUID groupId
    );

    @EntityGraph(
            attributePaths = {
                    "group",
                    "group.owner"
            }
    )
    Page<GroupMember> findByUser_Id(
            UUID userId,
            Pageable pageable
    );

    @EntityGraph(
            attributePaths = {
                    "group",
                    "group.owner"
            }
    )
    Page<GroupMember>
    findByUser_IdAndGroup_NameContainingIgnoreCase(
            UUID userId,
            String groupName,
            Pageable pageable
    );
}