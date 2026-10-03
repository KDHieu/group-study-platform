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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudyGroupServiceTest {

    @Mock
    private StudyGroupRepository studyGroupRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GroupMemberRepository groupMemberRepository;

    private StudyGroupService studyGroupService;

    @BeforeEach
    void setUp() {
        studyGroupService = new StudyGroupService(
                studyGroupRepository,
                userRepository,
                groupMemberRepository
        );
    }

    @Test
    void createGroup_shouldCreateGroupForAuthenticatedUser() {
        UUID userId = UUID.randomUUID();

        User owner = mock(User.class);

        when(owner.getId()).thenReturn(userId);
        when(owner.getUsername()).thenReturn("hieu");

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(owner));

        when(studyGroupRepository.save(any(StudyGroup.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        CreateGroupRequest request =
                new CreateGroupRequest(
                        "  Software Architecture  ",
                        "  Study software architecture together  "
                );

        GroupResponse response =
                studyGroupService.createGroup(
                        userId,
                        request
                );

        assertEquals(
                "Software Architecture",
                response.name()
        );

        assertEquals(
                "Study software architecture together",
                response.description()
        );

        assertEquals(
                userId,
                response.ownerId()
        );

        assertEquals(
                "hieu",
                response.ownerUsername()
        );

        ArgumentCaptor<StudyGroup> groupCaptor =
                ArgumentCaptor.forClass(
                        StudyGroup.class
                );

        verify(studyGroupRepository)
                .save(groupCaptor.capture());

        StudyGroup savedGroup =
                groupCaptor.getValue();

        assertEquals(
                "Software Architecture",
                savedGroup.getName()
        );

        assertEquals(
                owner,
                savedGroup.getOwner()
        );

        ArgumentCaptor<GroupMember> memberCaptor =
                ArgumentCaptor.forClass(
                        GroupMember.class
                );

        verify(groupMemberRepository)
                .save(memberCaptor.capture());

        GroupMember membership =
                memberCaptor.getValue();

        assertEquals(
                owner,
                membership.getUser()
        );

        assertEquals(
                GroupMemberRole.OWNER,
                membership.getRole()
        );

        assertEquals(
                savedGroup,
                membership.getGroup()
        );
    }

    @Test
    void createGroup_shouldThrowUnauthorizedWhenUserDoesNotExist() {
        UUID userId = UUID.randomUUID();

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        CreateGroupRequest request =
                new CreateGroupRequest(
                        "Software Architecture",
                        null
                );

        assertThrows(
                UnauthorizedException.class,
                () -> studyGroupService.createGroup(
                        userId,
                        request
                )
        );

        verify(
                studyGroupRepository,
                never()
        ).save(any());
    }

    @Test
    void getGroups_shouldSearchByNameWhenSearchIsProvided() {
        Pageable pageable =
                PageRequest.of(
                        0,
                        10
                );

        User owner = mock(User.class);
        StudyGroup group = mock(StudyGroup.class);

        when(group.getOwner())
                .thenReturn(owner);

        Page<StudyGroup> page =
                new PageImpl<>(
                        List.of(group),
                        pageable,
                        1
                );

        when(
                studyGroupRepository
                        .findByNameContainingIgnoreCase(
                                "architecture",
                                pageable
                        )
        ).thenReturn(page);

        Page<GroupResponse> result =
                studyGroupService.getGroups(
                        "  architecture  ",
                        pageable
                );

        assertEquals(
                1,
                result.getTotalElements()
        );

        verify(studyGroupRepository)
                .findByNameContainingIgnoreCase(
                        "architecture",
                        pageable
                );

        verify(
                studyGroupRepository,
                never()
        ).findAll(
                any(Pageable.class)
        );
    }

    @Test
    void getGroups_shouldReturnAllGroupsWhenSearchIsBlank() {
        Pageable pageable =
                PageRequest.of(
                        0,
                        10
                );

        Page<StudyGroup> page =
                new PageImpl<>(
                        List.of(),
                        pageable,
                        0
                );

        when(
                studyGroupRepository.findAll(pageable)
        ).thenReturn(page);

        Page<GroupResponse> result =
                studyGroupService.getGroups(
                        "   ",
                        pageable
                );

        assertEquals(
                0,
                result.getTotalElements()
        );

        verify(studyGroupRepository)
                .findAll(pageable);

        verify(
                studyGroupRepository,
                never()
        ).findByNameContainingIgnoreCase(
                anyString(),
                any(Pageable.class)
        );
    }

    @Test
    void getGroupById_shouldReturnGroupWhenGroupExists() {
        UUID groupId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();

        User owner = mock(User.class);
        StudyGroup group = mock(StudyGroup.class);

        when(owner.getId())
                .thenReturn(ownerId);

        when(owner.getUsername())
                .thenReturn("hieu");

        when(group.getId())
                .thenReturn(groupId);

        when(group.getName())
                .thenReturn("AI Study Group");

        when(group.getDescription())
                .thenReturn("Learn AI together");

        when(group.getOwner())
                .thenReturn(owner);

        when(studyGroupRepository.findById(groupId))
                .thenReturn(Optional.of(group));

        GroupResponse response =
                studyGroupService.getGroupById(
                        groupId
                );

        assertEquals(
                groupId,
                response.id()
        );

        assertEquals(
                "AI Study Group",
                response.name()
        );

        assertEquals(
                ownerId,
                response.ownerId()
        );

        assertEquals(
                "hieu",
                response.ownerUsername()
        );
    }

    @Test
    void getGroupById_shouldThrowNotFoundWhenGroupDoesNotExist() {
        UUID groupId = UUID.randomUUID();

        when(studyGroupRepository.findById(groupId))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> studyGroupService
                        .getGroupById(groupId)
        );
    }

    @Test
    void deleteGroup_shouldDeleteWhenCurrentUserIsOwner() {
        UUID groupId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();

        User owner = mock(User.class);
        StudyGroup group = mock(StudyGroup.class);

        when(owner.getId())
                .thenReturn(ownerId);

        when(group.getOwner())
                .thenReturn(owner);

        when(studyGroupRepository.findById(groupId))
                .thenReturn(Optional.of(group));

        studyGroupService.deleteGroup(
                groupId,
                ownerId
        );

        verify(studyGroupRepository)
                .delete(group);
    }

    @Test
    void deleteGroup_shouldThrowForbiddenWhenCurrentUserIsNotOwner() {
        UUID groupId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID anotherUserId = UUID.randomUUID();

        User owner = mock(User.class);
        StudyGroup group = mock(StudyGroup.class);

        when(owner.getId())
                .thenReturn(ownerId);

        when(group.getOwner())
                .thenReturn(owner);

        when(studyGroupRepository.findById(groupId))
                .thenReturn(Optional.of(group));

        assertThrows(
                ForbiddenException.class,
                () -> studyGroupService.deleteGroup(
                        groupId,
                        anotherUserId
                )
        );

        verify(
                studyGroupRepository,
                never()
        ).delete(any());
    }

    @Test
    void deleteGroup_shouldThrowNotFoundWhenGroupDoesNotExist() {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(studyGroupRepository.findById(groupId))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> studyGroupService.deleteGroup(
                        groupId,
                        userId
                )
        );

        verify(
                studyGroupRepository,
                never()
        ).delete(any());
    }

    @Test
    void joinGroup_shouldCreateMemberMembership() {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        StudyGroup group =
                mock(StudyGroup.class);

        User user =
                mock(User.class);

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

        studyGroupService.joinGroup(
                groupId,
                userId
        );

        ArgumentCaptor<GroupMember> captor =
                ArgumentCaptor.forClass(
                        GroupMember.class
                );

        verify(groupMemberRepository)
                .save(captor.capture());

        GroupMember membership =
                captor.getValue();

        assertEquals(
                group,
                membership.getGroup()
        );

        assertEquals(
                user,
                membership.getUser()
        );

        assertEquals(
                GroupMemberRole.MEMBER,
                membership.getRole()
        );
    }

    @Test
    void joinGroup_shouldThrowConflictWhenAlreadyMember() {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        StudyGroup group =
                mock(StudyGroup.class);

        User user =
                mock(User.class);

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
                () -> studyGroupService.joinGroup(
                        groupId,
                        userId
                )
        );

        verify(
                groupMemberRepository,
                never()
        ).save(any());
    }

    @Test
    void joinGroup_shouldThrowNotFoundWhenGroupDoesNotExist() {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(studyGroupRepository.findById(groupId))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> studyGroupService.joinGroup(
                        groupId,
                        userId
                )
        );

        verify(
                userRepository,
                never()
        ).findById(any());

        verify(
                groupMemberRepository,
                never()
        ).save(any());
    }

    @Test
    void leaveGroup_shouldDeleteMemberMembership() {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        StudyGroup group =
                mock(StudyGroup.class);

        GroupMember membership =
                mock(GroupMember.class);

        when(studyGroupRepository.findById(groupId))
                .thenReturn(Optional.of(group));

        when(
                groupMemberRepository
                        .findByGroup_IdAndUser_Id(
                                groupId,
                                userId
                        )
        ).thenReturn(
                Optional.of(membership)
        );

        when(membership.getRole())
                .thenReturn(
                        GroupMemberRole.MEMBER
                );

        studyGroupService.leaveGroup(
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

        StudyGroup group =
                mock(StudyGroup.class);

        GroupMember membership =
                mock(GroupMember.class);

        when(studyGroupRepository.findById(groupId))
                .thenReturn(Optional.of(group));

        when(
                groupMemberRepository
                        .findByGroup_IdAndUser_Id(
                                groupId,
                                userId
                        )
        ).thenReturn(
                Optional.of(membership)
        );

        when(membership.getRole())
                .thenReturn(
                        GroupMemberRole.OWNER
                );

        assertThrows(
                ConflictException.class,
                () -> studyGroupService.leaveGroup(
                        groupId,
                        userId
                )
        );

        verify(
                groupMemberRepository,
                never()
        ).delete(any());
    }

    @Test
    void leaveGroup_shouldThrowNotFoundWhenUserIsNotMember() {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        StudyGroup group =
                mock(StudyGroup.class);

        when(studyGroupRepository.findById(groupId))
                .thenReturn(Optional.of(group));

        when(
                groupMemberRepository
                        .findByGroup_IdAndUser_Id(
                                groupId,
                                userId
                        )
        ).thenReturn(
                Optional.empty()
        );

        assertThrows(
                NotFoundException.class,
                () -> studyGroupService.leaveGroup(
                        groupId,
                        userId
                )
        );

        verify(
                groupMemberRepository,
                never()
        ).delete(any());
    }

    @Test
    void leaveGroup_shouldThrowNotFoundWhenGroupDoesNotExist() {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(studyGroupRepository.findById(groupId))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> studyGroupService.leaveGroup(
                        groupId,
                        userId
                )
        );

        verify(
                groupMemberRepository,
                never()
        ).findByGroup_IdAndUser_Id(
                any(UUID.class),
                any(UUID.class)
        );
    }

    @Test
    void getMembers_shouldReturnMembersOrderedByJoinTime() {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        StudyGroup group =
                mock(StudyGroup.class);

        User user =
                mock(User.class);

        GroupMember member =
                mock(GroupMember.class);

        when(studyGroupRepository.findById(groupId))
                .thenReturn(Optional.of(group));

        when(
                groupMemberRepository
                        .findByGroup_IdOrderByJoinedAtAsc(
                                groupId
                        )
        ).thenReturn(
                List.of(member)
        );

        when(member.getUser())
                .thenReturn(user);

        when(user.getId())
                .thenReturn(userId);

        when(user.getUsername())
                .thenReturn("hieu");

        when(member.getRole())
                .thenReturn(
                        GroupMemberRole.MEMBER
                );

        List<GroupMemberResponse> result =
                studyGroupService.getMembers(
                        groupId
                );

        assertEquals(
                1,
                result.size()
        );

        assertEquals(
                userId,
                result.get(0).userId()
        );

        assertEquals(
                "hieu",
                result.get(0).username()
        );

        assertEquals(
                GroupMemberRole.MEMBER,
                result.get(0).role()
        );
    }

    @Test
    void getMembers_shouldThrowNotFoundWhenGroupDoesNotExist() {
        UUID groupId = UUID.randomUUID();

        when(studyGroupRepository.findById(groupId))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> studyGroupService
                        .getMembers(groupId)
        );

        verify(
                groupMemberRepository,
                never()
        ).findByGroup_IdOrderByJoinedAtAsc(
                any(UUID.class)
        );
    }
}