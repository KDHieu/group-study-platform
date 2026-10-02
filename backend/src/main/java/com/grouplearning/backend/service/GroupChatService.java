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
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

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

    private final ObjectStorageService objectStorageService;

    public GroupChatService(
            ChatMessageRepository chatMessageRepository,
            StudyGroupRepository studyGroupRepository,
            GroupMemberRepository groupMemberRepository,
            UserRepository userRepository,
            ObjectStorageService objectStorageService
    ) {
        this.chatMessageRepository =
                chatMessageRepository;

        this.studyGroupRepository =
                studyGroupRepository;

        this.groupMemberRepository =
                groupMemberRepository;

        this.userRepository =
                userRepository;

        this.objectStorageService =
                objectStorageService;
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

    @Transactional
    public ChatMessageResponse sendAudioMessage(
            UUID groupId,
            UUID userId,
            MultipartFile audio,
            Integer durationMs
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

        if (durationMs == null
                || durationMs <= 0) {

            throw new IllegalArgumentException(
                    "Audio duration must be greater than zero"
            );
        }

        if (durationMs > 5 * 60 * 1000) {
            throw new IllegalArgumentException(
                    "Voice message must not exceed 5 minutes"
            );
        }

        String objectKey =
                objectStorageService
                        .uploadAudio(
                                userId,
                                audio
                        );

        registerStorageCleanupOnRollback(
                objectKey
        );

        ChatMessage message =
                new ChatMessage(
                        group,
                        sender,
                        ChatMessageType.AUDIO,
                        null,
                        objectKey,
                        durationMs
                );

        ChatMessage saved =
                chatMessageRepository.save(
                        message
                );

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public ChatAudioContent getAudioContent(
            UUID groupId,
            UUID userId,
            UUID messageId
    ) {
        ensureGroupExists(groupId);
        ensureMember(groupId, userId);

        ChatMessage message =
                chatMessageRepository
                        .findById(messageId)
                        .orElseThrow(
                                () -> new NotFoundException(
                                        "Chat message not found"
                                )
                        );

        if (!message.getGroup().getId().equals(groupId)) {
            throw new NotFoundException(
                    "Chat message not found in this study group"
            );
        }

        if (message.getType() != ChatMessageType.AUDIO) {
            throw new IllegalArgumentException(
                    "Chat message is not an audio message"
            );
        }

        if (message.getMediaUrl() == null
                || message.getMediaUrl().isBlank()) {

            throw new NotFoundException(
                    "Audio file not found"
            );
        }

        var storedObject =
                objectStorageService.getObject(
                        message.getMediaUrl()
                );

        return new ChatAudioContent(
                storedObject.content(),
                storedObject.contentType()
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

        String mediaUrl =
                message.getMediaUrl();

        if (message.getType()
                == ChatMessageType.AUDIO
                && mediaUrl != null
                && !mediaUrl.isBlank()) {

            mediaUrl =
                    "/api/groups/"
                            + message.getGroup().getId()
                            + "/messages/"
                            + message.getId()
                            + "/audio";
        }

        return new ChatMessageResponse(
                message.getId(),
                message.getGroup().getId(),
                toSenderResponse(sender),
                message.getType(),
                message.getContent(),
                mediaUrl,
                message.getDurationMs(),
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

    public record ChatAudioContent(
            byte[] content,
            String contentType
    ) {
    }

    private static final Logger log =
            LoggerFactory.getLogger(
                    GroupChatService.class
            );

    private void registerStorageCleanupOnRollback(
            String objectKey
    ) {
        if (!TransactionSynchronizationManager
                .isSynchronizationActive()) {

            return;
        }

        TransactionSynchronizationManager
                .registerSynchronization(
                        new TransactionSynchronization() {

                            @Override
                            public void afterCompletion(
                                    int status
                            ) {
                                if (status
                                        != STATUS_ROLLED_BACK) {

                                    return;
                                }

                                try {
                                    objectStorageService
                                            .deleteObject(
                                                    objectKey
                                            );

                                } catch (Exception exception) {
                                    log.error(
                                            "Failed to remove orphaned audio object {} after transaction rollback",
                                            objectKey,
                                            exception
                                    );
                                }
                            }
                        }
                );
    }
}