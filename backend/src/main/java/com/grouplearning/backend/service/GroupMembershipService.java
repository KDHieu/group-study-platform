package com.grouplearning.backend.service;

import com.grouplearning.backend.entity.GroupMember;
import com.grouplearning.backend.entity.GroupMemberRole;
import com.grouplearning.backend.entity.StudyGroup;
import com.grouplearning.backend.entity.User;
import com.grouplearning.backend.exception.ConflictException;
import com.grouplearning.backend.exception.NotFoundException;
import com.grouplearning.backend.exception.UnauthorizedException;
import com.grouplearning.backend.repository.GroupMemberRepository;
import com.grouplearning.backend.repository.StudyGroupRepository;
import com.grouplearning.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.grouplearning.backend.dto.response.GroupMemberResponse;

import java.util.List;
import java.util.UUID;

@Service
public class GroupMembershipService {

    private final GroupMemberRepository groupMemberRepository;
    private final StudyGroupRepository studyGroupRepository;
    private final UserRepository userRepository;

    public GroupMembershipService(
            GroupMemberRepository groupMemberRepository,
            StudyGroupRepository studyGroupRepository,
            UserRepository userRepository
    ) {
        this.groupMemberRepository = groupMemberRepository;
        this.studyGroupRepository = studyGroupRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void joinGroup(
            UUID groupId,
            UUID currentUserId
    ) {
        StudyGroup group = studyGroupRepository
                .findById(groupId)
                .orElseThrow(() ->
                        new NotFoundException(
                                "Study group not found"
                        )
                );

        User user = userRepository
                .findById(currentUserId)
                .orElseThrow(() ->
                        new UnauthorizedException(
                                "Authenticated user no longer exists"
                        )
                );

        boolean alreadyMember =
                groupMemberRepository
                        .existsByGroup_IdAndUser_Id(
                                groupId,
                                currentUserId
                        );

        if (alreadyMember) {
            throw new ConflictException(
                    "User is already a member of this group"
            );
        }

        GroupMember membership =
                new GroupMember(
                        group,
                        user,
                        GroupMemberRole.MEMBER
                );

        groupMemberRepository.save(membership);
    }

    @Transactional
    public void leaveGroup(
            UUID groupId,
            UUID currentUserId
    ) {
        if (!studyGroupRepository.existsById(groupId)) {
            throw new NotFoundException(
                    "Study group not found"
            );
        }

        GroupMember membership =
                groupMemberRepository
                        .findByGroup_IdAndUser_Id(
                                groupId,
                                currentUserId
                        )
                        .orElseThrow(() ->
                                new NotFoundException(
                                        "User is not a member of this group"
                                )
                        );

        if (membership.getRole() == GroupMemberRole.OWNER) {
            throw new ConflictException(
                    "Group owner cannot leave the group"
            );
        }

        groupMemberRepository.delete(membership);
    }

    @Transactional(readOnly = true)
    public List<GroupMemberResponse> getMembers(
            UUID groupId
    ) {
        if (!studyGroupRepository.existsById(groupId)) {
            throw new NotFoundException(
                    "Study group not found"
            );
        }

        return groupMemberRepository
                .findByGroup_IdOrderByJoinedAtAsc(groupId)
                .stream()
                .map(member ->
                        new GroupMemberResponse(
                                member.getUser().getId(),
                                member.getUser().getUsername(),
                                member.getRole(),
                                member.getJoinedAt()
                        )
                )
                .toList();
    }
}