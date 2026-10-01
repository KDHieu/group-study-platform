package com.grouplearning.backend.service;

import com.grouplearning.backend.dto.response.ChatMessageResponse;
import com.grouplearning.backend.dto.response.ChatSenderResponse;
import com.grouplearning.backend.dto.response.TypingEventResponse;
import com.grouplearning.backend.entity.ChatMessage;
import com.grouplearning.backend.entity.ChatMessageType;
import com.grouplearning.backend.entity.StudyGroup;
import com.grouplearning.backend.entity.User;
import com.grouplearning.backend.exception.ForbiddenException;
import com.grouplearning.backend.exception.NotFoundException;
import com.grouplearning.backend.repository.ChatMessageRepository;
import com.grouplearning.backend.repository.GroupMemberRepository;
import com.grouplearning.backend.repository.StudyGroupRepository;
import com.grouplearning.backend.repository.UserRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class GroupChatService {

    private final ChatMessageRepository chatMessageRepository;

    private final StudyGroupRepository studyGroupRepository;

    private final GroupMemberRepository groupMemberRepository;

    private final UserRepository userRepository;

    public GroupChatService(
            ChatMessageRepository chatMessageRepository,
            StudyGroupRepository studyGroupRepository,
            GroupMemberRepository groupMemberRepository,
            UserRepository userRepository
    ) {
        this.chatMessageRepository =
                chatMessageRepository;

        this.studyGroupRepository =
                studyGroupRepository;

        this.groupMemberRepository =
                groupMemberRepository;

        this.userRepository =
                userRepository;
    }

    @Transactional(readOnly = true)
    public Page<ChatMessageResponse> getMessageHistory(
            UUID groupId,
            UUID userId,
            Pageable pageable
    ) {
        ensureGroupExists(groupId);
        ensureMember(groupId, userId);

        return chatMessageRepository
                .findByGroup_Id(
                        groupId,
                        pageable
                )
                .map(this::toResponse);
    }

    @Transactional
    public ChatMessageResponse sendTextMessage(
            UUID groupId,
            UUID userId,
            String content
    ) {
        StudyGroup group =
                studyGroupRepository
                        .findById(groupId)
                        .orElseThrow(
                                () -> new NotFoundException(
                                        "Study group not found"
                                )
                        );

        ensureMember(
                groupId,
                userId
        );

        User sender =
                userRepository
                        .findById(userId)
                        .orElseThrow(
                                () -> new NotFoundException(
                                        "User not found"
                                )
                        );

        String normalizedContent =
                content == null
                        ? ""
                        : content.trim();

        if (normalizedContent.isBlank()) {
            throw new IllegalArgumentException(
                    "Message content must not be blank"
            );
        }

        if (normalizedContent.length() > 2000) {
            throw new IllegalArgumentException(
                    "Message content must not exceed 2000 characters"
            );
        }

        ChatMessage message =
                new ChatMessage(
                        group,
                        sender,
                        ChatMessageType.TEXT,
                        normalizedContent,
                        null
                );

        ChatMessage saved =
                chatMessageRepository.save(
                        message
                );

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public TypingEventResponse createTypingEvent(
            UUID groupId,
            UUID userId,
            boolean typing
    ) {
        ensureGroupExists(groupId);
        ensureMember(groupId, userId);

        User user =
                userRepository
                        .findById(userId)
                        .orElseThrow(
                                () -> new NotFoundException(
                                        "User not found"
                                )
                        );

        return new TypingEventResponse(
                groupId,
                user.getId(),
                user.getUsername(),
                user.getDisplayName(),
                typing
        );
    }

    private void ensureGroupExists(
            UUID groupId
    ) {
        if (!studyGroupRepository
                .existsById(groupId)) {

            throw new NotFoundException(
                    "Study group not found"
            );
        }
    }

    private void ensureMember(
            UUID groupId,
            UUID userId
    ) {
        boolean member =
                groupMemberRepository
                        .existsByGroup_IdAndUser_Id(
                                groupId,
                                userId
                        );

        if (!member) {
            throw new ForbiddenException(
                    "You must be a member of this study group to access chat"
            );
        }
    }

    private ChatMessageResponse toResponse(
            ChatMessage message
    ) {
        User sender =
                message.getSender();

        return new ChatMessageResponse(
                message.getId(),
                message.getGroup().getId(),
                toSenderResponse(sender),
                message.getType(),
                message.getContent(),
                message.getMediaUrl(),
                message.getCreatedAt()
        );
    }

    private ChatSenderResponse toSenderResponse(
            User user
    ) {
        String avatarUrl = null;

        if (user.getAvatarUrl() != null
                && !user.getAvatarUrl().isBlank()) {

            avatarUrl =
                    "/api/users/"
                            + user.getId()
                            + "/avatar";
        }

        return new ChatSenderResponse(
                user.getId(),
                user.getUsername(),
                user.getDisplayName(),
                avatarUrl
        );
    }
}