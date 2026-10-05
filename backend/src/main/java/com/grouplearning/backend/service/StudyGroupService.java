package com.grouplearning.backend.service;

import com.grouplearning.backend.dto.request.CreateGroupRequest;
import com.grouplearning.backend.dto.response.GroupJoinRequestResponse;
import com.grouplearning.backend.dto.response.GroupMemberResponse;
import com.grouplearning.backend.dto.response.GroupResponse;
import com.grouplearning.backend.entity.GroupJoinRequest;
import com.grouplearning.backend.entity.GroupJoinRequestStatus;
import com.grouplearning.backend.entity.GroupMember;
import com.grouplearning.backend.entity.GroupMemberRole;
import com.grouplearning.backend.entity.GroupVisibility;
import com.grouplearning.backend.entity.StudyGroup;
import com.grouplearning.backend.entity.User;
import com.grouplearning.backend.exception.ConflictException;
import com.grouplearning.backend.exception.ForbiddenException;
import com.grouplearning.backend.exception.NotFoundException;
import com.grouplearning.backend.exception.UnauthorizedException;
import com.grouplearning.backend.repository.GroupJoinRequestRepository;
import com.grouplearning.backend.repository.GroupMemberRepository;
import com.grouplearning.backend.repository.StudyGroupRepository;
import com.grouplearning.backend.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class StudyGroupService {

    private final StudyGroupRepository studyGroupRepository;
    private final UserRepository userRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final GroupJoinRequestRepository groupJoinRequestRepository;

    public StudyGroupService(
            StudyGroupRepository studyGroupRepository,
            UserRepository userRepository,
            GroupMemberRepository groupMemberRepository,
            GroupJoinRequestRepository groupJoinRequestRepository
    ) {
        this.studyGroupRepository = studyGroupRepository;
        this.userRepository = userRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.groupJoinRequestRepository = groupJoinRequestRepository;
    }

    @Transactional
    public GroupResponse createGroup(
            UUID currentUserId,
            CreateGroupRequest request
    ) {
        User owner =
                findAuthenticatedUser(currentUserId);

        String name =
                request.name().trim();

        String description =
                request.description() == null
                        ? null
                        : request.description().trim();

        GroupVisibility visibility =
                request.visibility() == null
                        ? GroupVisibility.PUBLIC
                        : request.visibility();

        StudyGroup group =
                new StudyGroup(
                        name,
                        description,
                        owner,
                        visibility
                );

        StudyGroup savedGroup =
                studyGroupRepository.save(group);

        GroupMember ownerMembership =
                new GroupMember(
                        savedGroup,
                        owner,
                        GroupMemberRole.OWNER
                );

        groupMemberRepository.save(
                ownerMembership
        );

        return toResponse(savedGroup);
    }

    @Transactional(readOnly = true)
    public Page<GroupResponse> getGroups(
            String search,
            Pageable pageable
    ) {
        String normalizedSearch =
                normalizeSearch(search);

        Page<StudyGroup> groups;

        if (normalizedSearch.isBlank()) {
            groups =
                    studyGroupRepository
                            .findByVisibility(
                                    GroupVisibility.PUBLIC,
                                    pageable
                            );
        } else {
            groups =
                    studyGroupRepository
                            .findByVisibilityAndNameContainingIgnoreCase(
                                    GroupVisibility.PUBLIC,
                                    normalizedSearch,
                                    pageable
                            );
        }

        return groups.map(
                this::toResponse
        );
    }

    @Transactional(readOnly = true)
    public Page<GroupResponse> getMyGroups(
            UUID currentUserId,
            String search,
            Pageable pageable
    ) {
        findAuthenticatedUser(
                currentUserId
        );

        String normalizedSearch =
                normalizeSearch(search);

        Page<GroupMember> memberships;

        if (normalizedSearch.isBlank()) {
            memberships =
                    groupMemberRepository
                            .findByUser_Id(
                                    currentUserId,
                                    pageable
                            );
        } else {
            memberships =
                    groupMemberRepository
                            .findByUser_IdAndGroup_NameContainingIgnoreCase(
                                    currentUserId,
                                    normalizedSearch,
                                    pageable
                            );
        }

        return memberships.map(
                membership ->
                        toResponse(
                                membership.getGroup()
                        )
        );
    }

    /**
     * Returns basic study-group metadata.
     *
     * PUBLIC:
     * any authenticated user may view.
     *
     * PRIVATE:
     * any authenticated user with the direct group URL may
     * view basic metadata so that they can request access.
     *
     * Private members, chat and video remain protected by
     * their own membership checks.
     */
    @Transactional(readOnly = true)
    public GroupResponse getGroupById(
            UUID groupId,
            UUID currentUserId
    ) {
        findAuthenticatedUser(
                currentUserId
        );

        StudyGroup group =
                findGroup(groupId);

        return toResponse(group);
    }

    /*
     * Compatibility method used by existing internal tests.
     *
     * Without authenticated user context, PRIVATE groups
     * remain inaccessible through this overload.
     */
    @Transactional(readOnly = true)
    GroupResponse getGroupById(
            UUID groupId
    ) {
        StudyGroup group =
                findGroup(groupId);

        ensurePublicWithoutUserContext(
                group
        );

        return toResponse(group);
    }

    @Transactional
    public void deleteGroup(
            UUID groupId,
            UUID currentUserId
    ) {
        StudyGroup group =
                findGroup(groupId);

        ensureOwner(
                group,
                currentUserId
        );

        studyGroupRepository.delete(
                group
        );
    }

    /**
     * PUBLIC:
     * creates membership immediately.
     *
     * PRIVATE:
     * creates or reopens a PENDING join request.
     */
    @Transactional
    public void joinGroup(
            UUID groupId,
            UUID currentUserId
    ) {
        StudyGroup group =
                findGroup(groupId);

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

        if (group.getVisibility()
                == GroupVisibility.PUBLIC) {

            GroupMember membership =
                    new GroupMember(
                            group,
                            user,
                            GroupMemberRole.MEMBER
                    );

            groupMemberRepository.save(
                    membership
            );

            return;
        }

        Optional<GroupJoinRequest> existingRequest =
                groupJoinRequestRepository
                        .findByGroup_IdAndRequester_Id(
                                groupId,
                                currentUserId
                        );

        if (existingRequest.isEmpty()) {
            GroupJoinRequest joinRequest =
                    new GroupJoinRequest(
                            group,
                            user
                    );

            groupJoinRequestRepository.save(
                    joinRequest
            );

            return;
        }

        GroupJoinRequest joinRequest =
                existingRequest.get();

        if (joinRequest.getStatus()
                == GroupJoinRequestStatus.PENDING) {

            throw new ConflictException(
                    "Join request is already pending"
            );
        }

        /*
         * REJECTED:
         * user may request access again.
         *
         * APPROVED without membership:
         * the user may have left the group after approval.
         */
        joinRequest.reopen();
    }

    @Transactional(readOnly = true)
    public Optional<GroupJoinRequestResponse>
    getMyJoinRequest(
            UUID groupId,
            UUID currentUserId
    ) {
        findGroup(groupId);
        findAuthenticatedUser(currentUserId);

        return groupJoinRequestRepository
                .findByGroup_IdAndRequester_Id(
                        groupId,
                        currentUserId
                )
                .map(
                        this::toJoinRequestResponse
                );
    }

    @Transactional(readOnly = true)
    public List<GroupJoinRequestResponse>
    getPendingJoinRequests(
            UUID groupId,
            UUID currentUserId
    ) {
        StudyGroup group =
                findGroup(groupId);

        ensureOwner(
                group,
                currentUserId
        );

        return groupJoinRequestRepository
                .findByGroup_IdAndStatusOrderByCreatedAtAsc(
                        groupId,
                        GroupJoinRequestStatus.PENDING
                )
                .stream()
                .map(
                        this::toJoinRequestResponse
                )
                .toList();
    }

    @Transactional
    public void approveJoinRequest(
            UUID groupId,
            UUID requestId,
            UUID currentUserId
    ) {
        StudyGroup group =
                findGroup(groupId);

        ensureOwner(
                group,
                currentUserId
        );

        GroupJoinRequest joinRequest =
                groupJoinRequestRepository
                        .findByIdAndGroup_Id(
                                requestId,
                                groupId
                        )
                        .orElseThrow(
                                () ->
                                        new NotFoundException(
                                                "Join request not found"
                                        )
                        );

        if (joinRequest.getStatus()
                != GroupJoinRequestStatus.PENDING) {

            throw new ConflictException(
                    "Only pending join requests can be approved"
            );
        }

        User requester =
                joinRequest.getRequester();

        boolean alreadyMember =
                groupMemberRepository
                        .existsByGroup_IdAndUser_Id(
                                groupId,
                                requester.getId()
                        );

        if (alreadyMember) {
            throw new ConflictException(
                    "Requester is already a member of this group"
            );
        }

        GroupMember membership =
                new GroupMember(
                        group,
                        requester,
                        GroupMemberRole.MEMBER
                );

        groupMemberRepository.save(
                membership
        );

        joinRequest.approve();
    }

    @Transactional
    public void rejectJoinRequest(
            UUID groupId,
            UUID requestId,
            UUID currentUserId
    ) {
        StudyGroup group =
                findGroup(groupId);

        ensureOwner(
                group,
                currentUserId
        );

        GroupJoinRequest joinRequest =
                groupJoinRequestRepository
                        .findByIdAndGroup_Id(
                                requestId,
                                groupId
                        )
                        .orElseThrow(
                                () ->
                                        new NotFoundException(
                                                "Join request not found"
                                        )
                        );

        if (joinRequest.getStatus()
                != GroupJoinRequestStatus.PENDING) {

            throw new ConflictException(
                    "Only pending join requests can be rejected"
            );
        }

        joinRequest.reject();
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
                        .orElseThrow(
                                () ->
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

        groupMemberRepository.delete(
                membership
        );
    }

    /**
     * Member list access is stricter than group metadata access.
     *
     * PUBLIC:
     * any authenticated user may view members.
     *
     * PRIVATE:
     * only OWNER/MEMBER users may view members.
     */
    @Transactional(readOnly = true)
    public List<GroupMemberResponse> getMembers(
            UUID groupId,
            UUID currentUserId
    ) {
        StudyGroup group =
                findGroup(groupId);

        ensureCanAccessPrivateContent(
                group,
                groupId,
                currentUserId
        );

        return getMemberResponses(
                groupId
        );
    }

    @Transactional(readOnly = true)
    List<GroupMemberResponse> getMembers(
            UUID groupId
    ) {
        StudyGroup group =
                findGroup(groupId);

        ensurePublicWithoutUserContext(
                group
        );

        return getMemberResponses(
                groupId
        );
    }

    private List<GroupMemberResponse> getMemberResponses(
            UUID groupId
    ) {
        return groupMemberRepository
                .findByGroup_IdOrderByJoinedAtAsc(
                        groupId
                )
                .stream()
                .map(
                        member ->
                                new GroupMemberResponse(
                                        member.getUser()
                                                .getId(),
                                        member.getUser()
                                                .getUsername(),
                                        member.getRole(),
                                        member.getJoinedAt()
                                )
                )
                .toList();
    }

    private void ensureCanAccessPrivateContent(
            StudyGroup group,
            UUID groupId,
            UUID currentUserId
    ) {
        if (group.getVisibility()
                != GroupVisibility.PRIVATE) {

            return;
        }

        boolean isMember =
                groupMemberRepository
                        .existsByGroup_IdAndUser_Id(
                                groupId,
                                currentUserId
                        );

        if (!isMember) {
            throw new ForbiddenException(
                    "Private group content is only accessible to its members"
            );
        }
    }

    private void ensurePublicWithoutUserContext(
            StudyGroup group
    ) {
        if (group.getVisibility()
                == GroupVisibility.PRIVATE) {

            throw new ForbiddenException(
                    "Private group requires authenticated access"
            );
        }
    }

    private void ensureOwner(
            StudyGroup group,
            UUID currentUserId
    ) {
        if (!group.getOwner()
                .getId()
                .equals(currentUserId)) {

            throw new ForbiddenException(
                    "Only the group owner can perform this operation"
            );
        }
    }

    private String normalizeSearch(
            String search
    ) {
        return search == null
                ? ""
                : search.trim();
    }

    private StudyGroup findGroup(
            UUID groupId
    ) {
        return studyGroupRepository
                .findById(groupId)
                .orElseThrow(
                        () ->
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
                .orElseThrow(
                        () ->
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
                group.getVisibility(),
                group.getCreatedAt()
        );
    }

    private GroupJoinRequestResponse
    toJoinRequestResponse(
            GroupJoinRequest joinRequest
    ) {
        return new GroupJoinRequestResponse(
                joinRequest.getId(),
                joinRequest.getGroup()
                        .getId(),
                joinRequest.getRequester()
                        .getId(),
                joinRequest.getRequester()
                        .getUsername(),
                joinRequest.getStatus(),
                joinRequest.getCreatedAt(),
                joinRequest.getUpdatedAt()
        );
    }
}