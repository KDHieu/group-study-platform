package com.grouplearning.backend.service;

import com.grouplearning.backend.entity.GroupMember;
import com.grouplearning.backend.entity.GroupMemberRole;
import com.grouplearning.backend.entity.StudyGroup;
import com.grouplearning.backend.entity.User;
import com.grouplearning.backend.exception.ConflictException;
import com.grouplearning.backend.exception.NotFoundException;
import com.grouplearning.backend.repository.GroupMemberRepository;
import com.grouplearning.backend.repository.StudyGroupRepository;
import com.grouplearning.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroupMembershipServiceTest {

    @Mock
    private GroupMemberRepository groupMemberRepository;

    @Mock
    private StudyGroupRepository studyGroupRepository;

    @Mock
    private UserRepository userRepository;

    private GroupMembershipService groupMembershipService;

    @BeforeEach
    void setUp() {
        groupMembershipService = new GroupMembershipService(
                groupMemberRepository,
                studyGroupRepository,
                userRepository
        );
    }

    @Test
    void joinGroup_shouldCreateMemberMembership() {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        StudyGroup group = mock(StudyGroup.class);
        User user = mock(User.class);

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

        groupMembershipService.joinGroup(
                groupId,
                userId
        );

        ArgumentCaptor<GroupMember> captor =
                ArgumentCaptor.forClass(GroupMember.class);

        verify(groupMemberRepository)
                .save(captor.capture());

        GroupMember membership = captor.getValue();

        assertEquals(group, membership.getGroup());
        assertEquals(user, membership.getUser());
        assertEquals(
                GroupMemberRole.MEMBER,
                membership.getRole()
        );
    }

    @Test
    void joinGroup_shouldThrowConflictWhenAlreadyMember() {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        StudyGroup group = mock(StudyGroup.class);
        User user = mock(User.class);

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
        ).thenReturn(true);

        assertThrows(
                ConflictException.class,
                () -> groupMembershipService.joinGroup(
                        groupId,
                        userId
                )
        );

        verify(groupMemberRepository, never())
                .save(any());
    }

    @Test
    void leaveGroup_shouldDeleteMemberMembership() {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        GroupMember membership =
                mock(GroupMember.class);

        when(studyGroupRepository.existsById(groupId))
                .thenReturn(true);

        when(
                groupMemberRepository
                        .findByGroup_IdAndUser_Id(
                                groupId,
                                userId
                        )
        ).thenReturn(Optional.of(membership));

        when(membership.getRole())
                .thenReturn(GroupMemberRole.MEMBER);

        groupMembershipService.leaveGroup(
                groupId,
                userId
        );

        verify(groupMemberRepository)
                .delete(membership);
    }

    @Test
    void leaveGroup_shouldThrowConflictWhenUserIsOwner() {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        GroupMember membership =
                mock(GroupMember.class);

        when(studyGroupRepository.existsById(groupId))
                .thenReturn(true);

        when(
                groupMemberRepository
                        .findByGroup_IdAndUser_Id(
                                groupId,
                                userId
                        )
        ).thenReturn(Optional.of(membership));

        when(membership.getRole())
                .thenReturn(GroupMemberRole.OWNER);

        assertThrows(
                ConflictException.class,
                () -> groupMembershipService.leaveGroup(
                        groupId,
                        userId
                )
        );

        verify(groupMemberRepository, never())
                .delete(any());
    }

    @Test
    void leaveGroup_shouldThrowNotFoundWhenUserIsNotMember() {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(studyGroupRepository.existsById(groupId))
                .thenReturn(true);

        when(
                groupMemberRepository
                        .findByGroup_IdAndUser_Id(
                                groupId,
                                userId
                        )
        ).thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> groupMembershipService.leaveGroup(
                        groupId,
                        userId
                )
        );

        verify(groupMemberRepository, never())
                .delete(any());
    }
}