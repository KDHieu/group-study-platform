package com.grouplearning.backend.service;

import com.grouplearning.backend.dto.response.GroupJoinRequestResponse;
import com.grouplearning.backend.entity.GroupJoinRequest;
import com.grouplearning.backend.entity.GroupJoinRequestStatus;
import com.grouplearning.backend.entity.GroupMember;
import com.grouplearning.backend.entity.GroupMemberRole;
import com.grouplearning.backend.entity.GroupVisibility;
import com.grouplearning.backend.entity.StudyGroup;
import com.grouplearning.backend.entity.User;
import com.grouplearning.backend.exception.ConflictException;
import com.grouplearning.backend.exception.ForbiddenException;
import com.grouplearning.backend.repository.GroupJoinRequestRepository;
import com.grouplearning.backend.repository.GroupMemberRepository;
import com.grouplearning.backend.repository.StudyGroupRepository;
import com.grouplearning.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudyGroupJoinRequestTest {

    @Mock
    private StudyGroupRepository studyGroupRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GroupMemberRepository groupMemberRepository;

    @Mock
    private GroupJoinRequestRepository groupJoinRequestRepository;

    private StudyGroupService studyGroupService;

    @BeforeEach
    void setUp() {
        studyGroupService =
                new StudyGroupService(
                        studyGroupRepository,
                        userRepository,
                        groupMemberRepository,
                        groupJoinRequestRepository
                );
    }

    @Test
    void privateJoin_shouldReopenRejectedRequest() {
        UUID groupId =
                UUID.randomUUID();

        UUID userId =
                UUID.randomUUID();

        StudyGroup group =
                mock(StudyGroup.class);

        User user =
                mock(User.class);

        GroupJoinRequest joinRequest =
                mock(GroupJoinRequest.class);

        when(studyGroupRepository.findById(groupId))
                .thenReturn(Optional.of(group));

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(
                groupMemberRepository
                        .existsByGroup_IdAndUser_Id(
                                groupId,
                                userId
                        )
        ).thenReturn(false);

        when(group.getVisibility())
                .thenReturn(
                        GroupVisibility.PRIVATE
                );

        when(
                groupJoinRequestRepository
                        .findByGroup_IdAndRequester_Id(
                                groupId,
                                userId
                        )
        ).thenReturn(
                Optional.of(joinRequest)
        );

        when(joinRequest.getStatus())
                .thenReturn(
                        GroupJoinRequestStatus.REJECTED
                );

        studyGroupService.joinGroup(
                groupId,
                userId
        );

        verify(joinRequest)
                .reopen();

        verify(
                groupJoinRequestRepository,
                never()
        ).save(any());

        verify(
                groupMemberRepository,
                never()
        ).save(any());
    }

    @Test
    void privateJoin_shouldReopenApprovedRequestWhenUserIsNoLongerMember() {
        UUID groupId =
                UUID.randomUUID();

        UUID userId =
                UUID.randomUUID();

        StudyGroup group =
                mock(StudyGroup.class);

        User user =
                mock(User.class);

        GroupJoinRequest joinRequest =
                mock(GroupJoinRequest.class);

        when(studyGroupRepository.findById(groupId))
                .thenReturn(Optional.of(group));

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(
                groupMemberRepository
                        .existsByGroup_IdAndUser_Id(
                                groupId,
                                userId
                        )
        ).thenReturn(false);

        when(group.getVisibility())
                .thenReturn(
                        GroupVisibility.PRIVATE
                );

        when(
                groupJoinRequestRepository
                        .findByGroup_IdAndRequester_Id(
                                groupId,
                                userId
                        )
        ).thenReturn(
                Optional.of(joinRequest)
        );

        when(joinRequest.getStatus())
                .thenReturn(
                        GroupJoinRequestStatus.APPROVED
                );

        studyGroupService.joinGroup(
                groupId,
                userId
        );

        verify(joinRequest)
                .reopen();

        verify(
                groupMemberRepository,
                never()
        ).save(any());
    }

    @Test
    void approveJoinRequest_shouldCreateMemberAndApproveRequest() {
        UUID groupId =
                UUID.randomUUID();

        UUID requestId =
                UUID.randomUUID();

        UUID ownerId =
                UUID.randomUUID();

        UUID requesterId =
                UUID.randomUUID();

        StudyGroup group =
                mock(StudyGroup.class);

        User owner =
                mock(User.class);

        User requester =
                mock(User.class);

        GroupJoinRequest joinRequest =
                mock(GroupJoinRequest.class);

        when(studyGroupRepository.findById(groupId))
                .thenReturn(Optional.of(group));

        when(group.getOwner())
                .thenReturn(owner);

        when(owner.getId())
                .thenReturn(ownerId);

        when(
                groupJoinRequestRepository
                        .findByIdAndGroup_Id(
                                requestId,
                                groupId
                        )
        ).thenReturn(
                Optional.of(joinRequest)
        );

        when(joinRequest.getStatus())
                .thenReturn(
                        GroupJoinRequestStatus.PENDING
                );

        when(joinRequest.getRequester())
                .thenReturn(requester);

        when(requester.getId())
                .thenReturn(requesterId);

        when(
                groupMemberRepository
                        .existsByGroup_IdAndUser_Id(
                                groupId,
                                requesterId
                        )
        ).thenReturn(false);

        studyGroupService.approveJoinRequest(
                groupId,
                requestId,
                ownerId
        );

        ArgumentCaptor<GroupMember> memberCaptor =
                ArgumentCaptor.forClass(
                        GroupMember.class
                );

        verify(groupMemberRepository)
                .save(
                        memberCaptor.capture()
                );

        GroupMember membership =
                memberCaptor.getValue();

        assertEquals(
                group,
                membership.getGroup()
        );

        assertEquals(
                requester,
                membership.getUser()
        );

        assertEquals(
                GroupMemberRole.MEMBER,
                membership.getRole()
        );

        verify(joinRequest)
                .approve();
    }

    @Test
    void approveJoinRequest_shouldRejectNonPendingRequest() {
        UUID groupId =
                UUID.randomUUID();

        UUID requestId =
                UUID.randomUUID();

        UUID ownerId =
                UUID.randomUUID();

        StudyGroup group =
                mock(StudyGroup.class);

        User owner =
                mock(User.class);

        GroupJoinRequest joinRequest =
                mock(GroupJoinRequest.class);

        when(studyGroupRepository.findById(groupId))
                .thenReturn(Optional.of(group));

        when(group.getOwner())
                .thenReturn(owner);

        when(owner.getId())
                .thenReturn(ownerId);

        when(
                groupJoinRequestRepository
                        .findByIdAndGroup_Id(
                                requestId,
                                groupId
                        )
        ).thenReturn(
                Optional.of(joinRequest)
        );

        when(joinRequest.getStatus())
                .thenReturn(
                        GroupJoinRequestStatus.REJECTED
                );

        ConflictException exception =
                assertThrows(
                        ConflictException.class,
                        () ->
                                studyGroupService
                                        .approveJoinRequest(
                                                groupId,
                                                requestId,
                                                ownerId
                                        )
                );

        assertEquals(
                "Only pending join requests can be approved",
                exception.getMessage()
        );

        verify(
                groupMemberRepository,
                never()
        ).save(any());

        verify(
                joinRequest,
                never()
        ).approve();
    }

    @Test
    void rejectJoinRequest_shouldRejectPendingRequest() {
        UUID groupId =
                UUID.randomUUID();

        UUID requestId =
                UUID.randomUUID();

        UUID ownerId =
                UUID.randomUUID();

        StudyGroup group =
                mock(StudyGroup.class);

        User owner =
                mock(User.class);

        GroupJoinRequest joinRequest =
                mock(GroupJoinRequest.class);

        when(studyGroupRepository.findById(groupId))
                .thenReturn(Optional.of(group));

        when(group.getOwner())
                .thenReturn(owner);

        when(owner.getId())
                .thenReturn(ownerId);

        when(
                groupJoinRequestRepository
                        .findByIdAndGroup_Id(
                                requestId,
                                groupId
                        )
        ).thenReturn(
                Optional.of(joinRequest)
        );

        when(joinRequest.getStatus())
                .thenReturn(
                        GroupJoinRequestStatus.PENDING
                );

        studyGroupService.rejectJoinRequest(
                groupId,
                requestId,
                ownerId
        );

        verify(joinRequest)
                .reject();
    }

    @Test
    void approveJoinRequest_shouldForbidNonOwner() {
        UUID groupId =
                UUID.randomUUID();

        UUID requestId =
                UUID.randomUUID();

        UUID ownerId =
                UUID.randomUUID();

        UUID anotherUserId =
                UUID.randomUUID();

        StudyGroup group =
                mock(StudyGroup.class);

        User owner =
                mock(User.class);

        when(studyGroupRepository.findById(groupId))
                .thenReturn(Optional.of(group));

        when(group.getOwner())
                .thenReturn(owner);

        when(owner.getId())
                .thenReturn(ownerId);

        ForbiddenException exception =
                assertThrows(
                        ForbiddenException.class,
                        () ->
                                studyGroupService
                                        .approveJoinRequest(
                                                groupId,
                                                requestId,
                                                anotherUserId
                                        )
                );

        assertEquals(
                "Only the group owner can perform this operation",
                exception.getMessage()
        );

        verify(
                groupJoinRequestRepository,
                never()
        ).findByIdAndGroup_Id(
                any(UUID.class),
                any(UUID.class)
        );
    }

    @Test
    void getPendingJoinRequests_shouldReturnOnlyPendingRequestsForOwner() {
        UUID groupId =
                UUID.randomUUID();

        UUID ownerId =
                UUID.randomUUID();

        UUID requestId =
                UUID.randomUUID();

        UUID requesterId =
                UUID.randomUUID();

        StudyGroup group =
                mock(StudyGroup.class);

        User owner =
                mock(User.class);

        User requester =
                mock(User.class);

        GroupJoinRequest joinRequest =
                mock(GroupJoinRequest.class);

        Instant createdAt =
                Instant.parse(
                        "2026-10-05T10:00:00Z"
                );

        Instant updatedAt =
                Instant.parse(
                        "2026-10-05T10:00:00Z"
                );

        when(studyGroupRepository.findById(groupId))
                .thenReturn(Optional.of(group));

        when(group.getOwner())
                .thenReturn(owner);

        when(owner.getId())
                .thenReturn(ownerId);

        when(
                groupJoinRequestRepository
                        .findByGroup_IdAndStatusOrderByCreatedAtAsc(
                                groupId,
                                GroupJoinRequestStatus.PENDING
                        )
        ).thenReturn(
                List.of(joinRequest)
        );

        when(joinRequest.getId())
                .thenReturn(requestId);

        when(joinRequest.getGroup())
                .thenReturn(group);

        when(group.getId())
                .thenReturn(groupId);

        when(joinRequest.getRequester())
                .thenReturn(requester);

        when(requester.getId())
                .thenReturn(requesterId);

        when(requester.getUsername())
                .thenReturn("student");

        when(joinRequest.getStatus())
                .thenReturn(
                        GroupJoinRequestStatus.PENDING
                );

        when(joinRequest.getCreatedAt())
                .thenReturn(createdAt);

        when(joinRequest.getUpdatedAt())
                .thenReturn(updatedAt);

        List<GroupJoinRequestResponse> result =
                studyGroupService
                        .getPendingJoinRequests(
                                groupId,
                                ownerId
                        );

        assertEquals(
                1,
                result.size()
        );

        GroupJoinRequestResponse response =
                result.get(0);

        assertEquals(
                requestId,
                response.id()
        );

        assertEquals(
                groupId,
                response.groupId()
        );

        assertEquals(
                requesterId,
                response.userId()
        );

        assertEquals(
                "student",
                response.username()
        );

        assertEquals(
                GroupJoinRequestStatus.PENDING,
                response.status()
        );
    }
}