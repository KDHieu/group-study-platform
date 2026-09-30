package com.grouplearning.backend.service;

import com.grouplearning.backend.entity.GroupMember;
import com.grouplearning.backend.entity.GroupMemberRole;
import com.grouplearning.backend.repository.GroupMemberRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import com.grouplearning.backend.dto.request.CreateGroupRequest;
import com.grouplearning.backend.dto.response.GroupResponse;
import com.grouplearning.backend.entity.StudyGroup;
import com.grouplearning.backend.entity.User;
import com.grouplearning.backend.exception.UnauthorizedException;
import com.grouplearning.backend.repository.StudyGroupRepository;
import com.grouplearning.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.grouplearning.backend.exception.ForbiddenException;
import com.grouplearning.backend.exception.NotFoundException;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudyGroupServiceTest {

    @Mock
    private StudyGroupRepository studyGroupRepository;

    @Mock
    private UserRepository userRepository;

    private StudyGroupService studyGroupService;

    @Mock
    private GroupMemberRepository groupMemberRepository;

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

        assertEquals(userId, response.ownerId());
        assertEquals("hieu", response.ownerUsername());

        ArgumentCaptor<StudyGroup> captor =
                ArgumentCaptor.forClass(StudyGroup.class);

        verify(studyGroupRepository).save(captor.capture());

        StudyGroup savedGroup = captor.getValue();

        assertEquals(
                "Software Architecture",
                savedGroup.getName()
        );

        assertEquals(owner, savedGroup.getOwner());

        ArgumentCaptor<GroupMember> memberCaptor =
                ArgumentCaptor.forClass(GroupMember.class);

        verify(groupMemberRepository)
                .save(memberCaptor.capture());

        GroupMember membership = memberCaptor.getValue();

        assertEquals(owner, membership.getUser());
        assertEquals(GroupMemberRole.OWNER, membership.getRole());
        assertEquals(savedGroup, membership.getGroup());
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

        verify(studyGroupRepository, never())
                .save(any());
    }

    @Test
    void getGroups_shouldSearchByNameWhenSearchIsProvided() {
        Pageable pageable = PageRequest.of(0, 10);

        User owner = mock(User.class);
        StudyGroup group = mock(StudyGroup.class);

        when(group.getOwner()).thenReturn(owner);

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

        assertEquals(1, result.getTotalElements());

        verify(studyGroupRepository)
                .findByNameContainingIgnoreCase(
                        "architecture",
                        pageable
                );

        verify(
                studyGroupRepository,
                never()
        ).findAll(any(Pageable.class));
    }

    @Test
    void getGroups_shouldReturnAllGroupsWhenSearchIsBlank() {
        Pageable pageable = PageRequest.of(0, 10);

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

        assertEquals(0, result.getTotalElements());

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
    void deleteGroup_shouldDeleteWhenCurrentUserIsOwner() {
        UUID groupId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();

        User owner = mock(User.class);
        StudyGroup group = mock(StudyGroup.class);

        when(owner.getId()).thenReturn(ownerId);
        when(group.getOwner()).thenReturn(owner);

        when(studyGroupRepository.findById(groupId))
                .thenReturn(Optional.of(group));

        studyGroupService.deleteGroup(
                groupId,
                ownerId
        );

        verify(studyGroupRepository).delete(group);
    }

    @Test
    void deleteGroup_shouldThrowForbiddenWhenCurrentUserIsNotOwner() {
        UUID groupId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID anotherUserId = UUID.randomUUID();

        User owner = mock(User.class);
        StudyGroup group = mock(StudyGroup.class);

        when(owner.getId()).thenReturn(ownerId);
        when(group.getOwner()).thenReturn(owner);

        when(studyGroupRepository.findById(groupId))
                .thenReturn(Optional.of(group));

        assertThrows(
                ForbiddenException.class,
                () -> studyGroupService.deleteGroup(
                        groupId,
                        anotherUserId
                )
        );

        verify(studyGroupRepository, never())
                .delete(any());
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

        verify(studyGroupRepository, never())
                .delete(any());
    }
}