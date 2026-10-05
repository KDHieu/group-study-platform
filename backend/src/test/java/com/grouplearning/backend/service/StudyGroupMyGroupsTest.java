package com.grouplearning.backend.service;

import com.grouplearning.backend.dto.response.GroupResponse;
import com.grouplearning.backend.entity.GroupMember;
import com.grouplearning.backend.entity.GroupVisibility;
import com.grouplearning.backend.entity.StudyGroup;
import com.grouplearning.backend.entity.User;
import com.grouplearning.backend.exception.UnauthorizedException;
import com.grouplearning.backend.repository.GroupJoinRequestRepository;
import com.grouplearning.backend.repository.GroupMemberRepository;
import com.grouplearning.backend.repository.StudyGroupRepository;
import com.grouplearning.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudyGroupMyGroupsTest {

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
    void getMyGroups_shouldReturnPublicAndPrivateMemberships() {
        UUID currentUserId =
                UUID.randomUUID();

        User currentUser =
                mock(User.class);

        when(userRepository.findById(currentUserId))
                .thenReturn(Optional.of(currentUser));

        Pageable pageable =
                PageRequest.of(
                        0,
                        10
                );

        GroupMember publicMembership =
                mock(GroupMember.class);

        GroupMember privateMembership =
                mock(GroupMember.class);

        StudyGroup publicGroup =
                createGroupMock(
                        "Public AI Group",
                        GroupVisibility.PUBLIC
                );

        StudyGroup privateGroup =
                createGroupMock(
                        "Private Architecture Group",
                        GroupVisibility.PRIVATE
                );

        when(publicMembership.getGroup())
                .thenReturn(publicGroup);

        when(privateMembership.getGroup())
                .thenReturn(privateGroup);

        Page<GroupMember> memberships =
                new PageImpl<>(
                        List.of(
                                publicMembership,
                                privateMembership
                        ),
                        pageable,
                        2
                );

        when(
                groupMemberRepository
                        .findByUser_Id(
                                currentUserId,
                                pageable
                        )
        ).thenReturn(memberships);

        Page<GroupResponse> result =
                studyGroupService.getMyGroups(
                        currentUserId,
                        "",
                        pageable
                );

        assertEquals(
                2,
                result.getTotalElements()
        );

        assertEquals(
                "Public AI Group",
                result.getContent()
                        .get(0)
                        .name()
        );

        assertEquals(
                GroupVisibility.PUBLIC,
                result.getContent()
                        .get(0)
                        .visibility()
        );

        assertEquals(
                "Private Architecture Group",
                result.getContent()
                        .get(1)
                        .name()
        );

        assertEquals(
                GroupVisibility.PRIVATE,
                result.getContent()
                        .get(1)
                        .visibility()
        );

        verify(groupMemberRepository)
                .findByUser_Id(
                        currentUserId,
                        pageable
                );

        verify(
                groupMemberRepository,
                never()
        ).findByUser_IdAndGroup_NameContainingIgnoreCase(
                currentUserId,
                "",
                pageable
        );
    }

    @Test
    void getMyGroups_shouldSearchOnlyWithinCurrentUsersMemberships() {
        UUID currentUserId =
                UUID.randomUUID();

        User currentUser =
                mock(User.class);

        when(userRepository.findById(currentUserId))
                .thenReturn(Optional.of(currentUser));

        Pageable pageable =
                PageRequest.of(
                        0,
                        10
                );

        GroupMember membership =
                mock(GroupMember.class);

        StudyGroup group =
                createGroupMock(
                        "Private AI Group",
                        GroupVisibility.PRIVATE
                );

        when(membership.getGroup())
                .thenReturn(group);

        Page<GroupMember> memberships =
                new PageImpl<>(
                        List.of(membership),
                        pageable,
                        1
                );

        when(
                groupMemberRepository
                        .findByUser_IdAndGroup_NameContainingIgnoreCase(
                                currentUserId,
                                "ai",
                                pageable
                        )
        ).thenReturn(memberships);

        Page<GroupResponse> result =
                studyGroupService.getMyGroups(
                        currentUserId,
                        "  ai  ",
                        pageable
                );

        assertEquals(
                1,
                result.getTotalElements()
        );

        assertEquals(
                "Private AI Group",
                result.getContent()
                        .get(0)
                        .name()
        );

        verify(groupMemberRepository)
                .findByUser_IdAndGroup_NameContainingIgnoreCase(
                        currentUserId,
                        "ai",
                        pageable
                );

        verify(
                groupMemberRepository,
                never()
        ).findByUser_Id(
                currentUserId,
                pageable
        );
    }

    @Test
    void getMyGroups_shouldThrowUnauthorizedWhenCurrentUserNoLongerExists() {
        UUID currentUserId =
                UUID.randomUUID();

        Pageable pageable =
                PageRequest.of(
                        0,
                        10
                );

        when(userRepository.findById(currentUserId))
                .thenReturn(Optional.empty());

        assertThrows(
                UnauthorizedException.class,
                () ->
                        studyGroupService.getMyGroups(
                                currentUserId,
                                "",
                                pageable
                        )
        );

        verify(
                groupMemberRepository,
                never()
        ).findByUser_Id(
                currentUserId,
                pageable
        );
    }

    private StudyGroup createGroupMock(
            String name,
            GroupVisibility visibility
    ) {
        StudyGroup group =
                mock(StudyGroup.class);

        User owner =
                mock(User.class);

        UUID groupId =
                UUID.randomUUID();

        UUID ownerId =
                UUID.randomUUID();

        when(group.getId())
                .thenReturn(groupId);

        when(group.getName())
                .thenReturn(name);

        when(group.getOwner())
                .thenReturn(owner);

        when(group.getVisibility())
                .thenReturn(visibility);

        when(owner.getId())
                .thenReturn(ownerId);

        when(owner.getUsername())
                .thenReturn("owner");

        return group;
    }
}