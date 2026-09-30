package com.grouplearning.backend.repository;

import com.grouplearning.backend.entity.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

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
}