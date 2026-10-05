package com.grouplearning.backend.service;

import com.grouplearning.backend.dto.response.GroupMemberResponse;
import com.grouplearning.backend.dto.response.GroupResponse;
import com.grouplearning.backend.entity.GroupMember;
import com.grouplearning.backend.entity.GroupMemberRole;
import com.grouplearning.backend.entity.GroupVisibility;
import com.grouplearning.backend.entity.StudyGroup;
import com.grouplearning.backend.entity.User;
import com.grouplearning.backend.exception.ForbiddenException;
import com.grouplearning.backend.repository.GroupJoinRequestRepository;
import com.grouplearning.backend.repository.GroupMemberRepository;
import com.grouplearning.backend.repository.StudyGroupRepository;
import com.grouplearning.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
class StudyGroupPrivateAccessTest {

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
    void getGroupById_shouldAllowPublicGroupWithoutMembership() {
        UUID groupId =
                UUID.randomUUID();

        UUID userId =
                UUID.randomUUID();

        User currentUser =
                mock(User.class);

        StudyGroup group =
                createResponseGroupMock(
                        groupId,
                        GroupVisibility.PUBLIC
                );

        when(userRepository.findById(userId))
                .thenReturn(
                        Optional.of(currentUser)
                );

        when(studyGroupRepository.findById(groupId))
                .thenReturn(
                        Optional.of(group)
                );

        GroupResponse response =
                studyGroupService.getGroupById(
                        groupId,
                        userId
                );

        assertEquals(
                groupId,
                response.id()
        );

        assertEquals(
                GroupVisibility.PUBLIC,
                response.visibility()
        );

        verify(
                groupMemberRepository,
                never()
        ).existsByGroup_IdAndUser_Id(
                any(UUID.class),
                any(UUID.class)
        );
    }

    @Test
    void getGroupById_shouldAllowPrivateGroupForMember() {
        UUID groupId =
                UUID.randomUUID();

        UUID userId =
                UUID.randomUUID();

        User currentUser =
                mock(User.class);

        StudyGroup group =
                createResponseGroupMock(
                        groupId,
                        GroupVisibility.PRIVATE
                );

        when(userRepository.findById(userId))
                .thenReturn(
                        Optional.of(currentUser)
                );

        when(studyGroupRepository.findById(groupId))
                .thenReturn(
                        Optional.of(group)
                );

        GroupResponse response =
                studyGroupService.getGroupById(
                        groupId,
                        userId
                );

        assertEquals(
                groupId,
                response.id()
        );

        assertEquals(
                GroupVisibility.PRIVATE,
                response.visibility()
        );
    }

    @Test
    void getGroupById_shouldAllowPrivateMetadataForNonMember() {
        UUID groupId =
                UUID.randomUUID();

        UUID userId =
                UUID.randomUUID();

        User currentUser =
                mock(User.class);

        StudyGroup group =
                createResponseGroupMock(
                        groupId,
                        GroupVisibility.PRIVATE
                );

        when(userRepository.findById(userId))
                .thenReturn(
                        Optional.of(currentUser)
                );

        when(studyGroupRepository.findById(groupId))
                .thenReturn(
                        Optional.of(group)
                );

        GroupResponse response =
                studyGroupService.getGroupById(
                        groupId,
                        userId
                );

        assertEquals(
                groupId,
                response.id()
        );

        assertEquals(
                GroupVisibility.PRIVATE,
                response.visibility()
        );

        verify(
                groupMemberRepository,
                never()
        ).existsByGroup_IdAndUser_Id(
                any(UUID.class),
                any(UUID.class)
        );
    }

    @Test
    void getMembers_shouldAllowPrivateGroupForMember() {
        UUID groupId =
                UUID.randomUUID();

        UUID currentUserId =
                UUID.randomUUID();

        UUID memberUserId =
                UUID.randomUUID();

        StudyGroup group =
                createAccessOnlyGroupMock(
                        GroupVisibility.PRIVATE
                );

        GroupMember member =
                mock(GroupMember.class);

        User memberUser =
                mock(User.class);

        when(studyGroupRepository.findById(groupId))
                .thenReturn(
                        Optional.of(group)
                );

        when(
                groupMemberRepository
                        .existsByGroup_IdAndUser_Id(
                                groupId,
                                currentUserId
                        )
        ).thenReturn(true);

        when(
                groupMemberRepository
                        .findByGroup_IdOrderByJoinedAtAsc(
                                groupId
                        )
        ).thenReturn(
                List.of(member)
        );

        when(member.getUser())
                .thenReturn(memberUser);

        when(memberUser.getId())
                .thenReturn(memberUserId);

        when(memberUser.getUsername())
                .thenReturn("member");

        when(member.getRole())
                .thenReturn(
                        GroupMemberRole.MEMBER
                );

        List<GroupMemberResponse> result =
                studyGroupService.getMembers(
                        groupId,
                        currentUserId
                );

        assertEquals(
                1,
                result.size()
        );

        assertEquals(
                memberUserId,
                result.get(0).userId()
        );

        assertEquals(
                "member",
                result.get(0).username()
        );

        assertEquals(
                GroupMemberRole.MEMBER,
                result.get(0).role()
        );
    }

    @Test
    void getMembers_shouldForbidPrivateGroupForNonMember() {
        UUID groupId =
                UUID.randomUUID();

        UUID currentUserId =
                UUID.randomUUID();

        StudyGroup group =
                createAccessOnlyGroupMock(
                        GroupVisibility.PRIVATE
                );

        when(studyGroupRepository.findById(groupId))
                .thenReturn(
                        Optional.of(group)
                );

        when(
                groupMemberRepository
                        .existsByGroup_IdAndUser_Id(
                                groupId,
                                currentUserId
                        )
        ).thenReturn(false);

        ForbiddenException exception =
                assertThrows(
                        ForbiddenException.class,
                        () ->
                                studyGroupService
                                        .getMembers(
                                                groupId,
                                                currentUserId
                                        )
                );

        assertEquals(
                "Private group content is only accessible to its members",
                exception.getMessage()
        );

        verify(
                groupMemberRepository,
                never()
        ).findByGroup_IdOrderByJoinedAtAsc(
                any(UUID.class)
        );
    }

    private StudyGroup createAccessOnlyGroupMock(
            GroupVisibility visibility
    ) {
        StudyGroup group =
                mock(StudyGroup.class);

        when(group.getVisibility())
                .thenReturn(visibility);

        return group;
    }

    private StudyGroup createResponseGroupMock(
            UUID groupId,
            GroupVisibility visibility
    ) {
        StudyGroup group =
                mock(StudyGroup.class);

        User owner =
                mock(User.class);

        UUID ownerId =
                UUID.randomUUID();

        when(group.getId())
                .thenReturn(groupId);

        when(group.getName())
                .thenReturn("Study Group");

        when(group.getDescription())
                .thenReturn("Description");

        when(group.getVisibility())
                .thenReturn(visibility);

        when(group.getOwner())
                .thenReturn(owner);

        when(owner.getId())
                .thenReturn(ownerId);

        when(owner.getUsername())
                .thenReturn("owner");

        return group;
    }
}