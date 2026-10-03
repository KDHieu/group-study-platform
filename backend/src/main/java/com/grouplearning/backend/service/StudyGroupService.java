package com.grouplearning.backend.service;

import com.grouplearning.backend.dto.request.CreateGroupRequest;
import com.grouplearning.backend.dto.response.GroupMemberResponse;
import com.grouplearning.backend.dto.response.GroupResponse;
import com.grouplearning.backend.entity.GroupMember;
import com.grouplearning.backend.entity.GroupMemberRole;
import com.grouplearning.backend.entity.StudyGroup;
import com.grouplearning.backend.entity.User;
import com.grouplearning.backend.exception.ConflictException;
import com.grouplearning.backend.exception.ForbiddenException;
import com.grouplearning.backend.exception.NotFoundException;
import com.grouplearning.backend.exception.UnauthorizedException;
import com.grouplearning.backend.repository.GroupMemberRepository;
import com.grouplearning.backend.repository.StudyGroupRepository;
import com.grouplearning.backend.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class StudyGroupService {

    private final StudyGroupRepository studyGroupRepository;
    private final UserRepository userRepository;
    private final GroupMemberRepository groupMemberRepository;

    public StudyGroupService(
            StudyGroupRepository studyGroupRepository,
            UserRepository userRepository,
            GroupMemberRepository groupMemberRepository
    ) {
        this.studyGroupRepository = studyGroupRepository;
        this.userRepository = userRepository;
        this.groupMemberRepository = groupMemberRepository;
    }

    @Transactional
    public GroupResponse createGroup(
            UUID currentUserId,
            CreateGroupRequest request
    ) {
        User owner = findAuthenticatedUser(currentUserId);

        String name = request.name().trim();

        String description =
                request.description() == null
                        ? null
                        : request.description().trim();

        StudyGroup group = new StudyGroup(
                name,
                description,
                owner
        );

        StudyGroup savedGroup =
                studyGroupRepository.save(group);

        GroupMember ownerMembership =
                new GroupMember(
                        savedGroup,
                        owner,
                        GroupMemberRole.OWNER
                );

        groupMemberRepository.save(ownerMembership);

        return toResponse(savedGroup);
    }

    @Transactional(readOnly = true)
    public Page<GroupResponse> getGroups(
            String search,
            Pageable pageable
    ) {
        String normalizedSearch =
                search == null
                        ? ""
                        : search.trim();

        Page<StudyGroup> groups;

        if (normalizedSearch.isBlank()) {
            groups =
                    studyGroupRepository.findAll(
                            pageable
                    );
        } else {
            groups =
                    studyGroupRepository
                            .findByNameContainingIgnoreCase(
                                    normalizedSearch,
                                    pageable
                            );
        }

        return groups.map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public GroupResponse getGroupById(
            UUID groupId
    ) {
        StudyGroup group = findGroup(groupId);

        return toResponse(group);
    }

    @Transactional
    public void deleteGroup(
            UUID groupId,
            UUID currentUserId
    ) {
        StudyGroup group = findGroup(groupId);

        if (!group.getOwner()
                .getId()
                .equals(currentUserId)) {

            throw new ForbiddenException(
                    "Only the group owner can delete this group"
            );
        }

        studyGroupRepository.delete(group);
    }

    @Transactional
    public void joinGroup(
            UUID groupId,
            UUID currentUserId
    ) {
        StudyGroup group = findGroup(groupId);

        User user =
                findAuthenticatedUser(
                        currentUserId
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
        findGroup(groupId);

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

        if (membership.getRole()
                == GroupMemberRole.OWNER) {

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
        findGroup(groupId);

        return groupMemberRepository
                .findByGroup_IdOrderByJoinedAtAsc(
                        groupId
                )
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

    private StudyGroup findGroup(
            UUID groupId
    ) {
        return studyGroupRepository
                .findById(groupId)
                .orElseThrow(() ->
                        new NotFoundException(
                                "Study group not found"
                        )
                );
    }

    private User findAuthenticatedUser(
            UUID userId
    ) {
        return userRepository
                .findById(userId)
                .orElseThrow(() ->
                        new UnauthorizedException(
                                "Authenticated user no longer exists"
                        )
                );
    }

    private GroupResponse toResponse(
            StudyGroup group
    ) {
        return new GroupResponse(
                group.getId(),
                group.getName(),
                group.getDescription(),
                group.getOwner().getId(),
                group.getOwner().getUsername(),
                group.getCreatedAt()
        );
    }
}