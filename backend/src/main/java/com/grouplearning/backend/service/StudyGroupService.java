package com.grouplearning.backend.service;

import com.grouplearning.backend.exception.ForbiddenException;
import com.grouplearning.backend.entity.GroupMember;
import com.grouplearning.backend.entity.GroupMemberRole;
import com.grouplearning.backend.repository.GroupMemberRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.grouplearning.backend.dto.request.CreateGroupRequest;
import com.grouplearning.backend.dto.response.GroupResponse;
import com.grouplearning.backend.entity.StudyGroup;
import com.grouplearning.backend.entity.User;
import com.grouplearning.backend.exception.UnauthorizedException;
import com.grouplearning.backend.repository.StudyGroupRepository;
import com.grouplearning.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.grouplearning.backend.exception.NotFoundException;

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
        User owner = userRepository.findById(currentUserId)
                .orElseThrow(() ->
                        new UnauthorizedException(
                                "Authenticated user no longer exists"
                        )
                );

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
            groups = studyGroupRepository.findAll(pageable);
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

    private GroupResponse toResponse(StudyGroup group) {
        return new GroupResponse(
                group.getId(),
                group.getName(),
                group.getDescription(),
                group.getOwner().getId(),
                group.getOwner().getUsername(),
                group.getCreatedAt()
        );
    }

    @Transactional(readOnly = true)
    public GroupResponse getGroupById(UUID groupId) {
        StudyGroup group = studyGroupRepository
                .findById(groupId)
                .orElseThrow(() ->
                        new NotFoundException(
                                "Study group not found"
                        )
                );

        return toResponse(group);
    }

    @Transactional
    public void deleteGroup(
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

        if (!group.getOwner().getId().equals(currentUserId)) {
            throw new ForbiddenException(
                    "Only the group owner can delete this group"
            );
        }

        studyGroupRepository.delete(group);
    }
}