package com.grouplearning.backend.service;

import com.grouplearning.backend.config.LiveKitProperties;
import com.grouplearning.backend.dto.request.CreateCallRoomRequest;
import com.grouplearning.backend.dto.response.CallRoomParticipantResponse;
import com.grouplearning.backend.dto.response.CallRoomResponse;
import com.grouplearning.backend.dto.response.ChatMessageResponse;
import com.grouplearning.backend.dto.response.ChatSenderResponse;
import com.grouplearning.backend.dto.response.TypingEventResponse;
import com.grouplearning.backend.dto.response.VideoTokenResponse;
import com.grouplearning.backend.entity.CallRoom;
import com.grouplearning.backend.entity.ChatMessage;
import com.grouplearning.backend.entity.ChatMessageType;
import com.grouplearning.backend.entity.StudyGroup;
import com.grouplearning.backend.entity.User;
import com.grouplearning.backend.exception.ForbiddenException;
import com.grouplearning.backend.exception.NotFoundException;
import com.grouplearning.backend.repository.CallRoomRepository;
import com.grouplearning.backend.repository.ChatMessageRepository;
import com.grouplearning.backend.repository.GroupMemberRepository;
import com.grouplearning.backend.repository.StudyGroupRepository;
import com.grouplearning.backend.repository.UserRepository;
import io.livekit.server.AccessToken;
import io.livekit.server.RoomJoin;
import io.livekit.server.RoomName;
import io.livekit.server.RoomServiceClient;
import livekit.LivekitModels;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;
import retrofit2.Response;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
public class StudyRoomService {

    private static final long TOKEN_TTL_MS =
            15 * 60 * 1000L;

    private static final String ROOM_PREFIX =
            "study-group-";

    private static final Logger log =
            LoggerFactory.getLogger(
                    StudyRoomService.class
            );

    private final ChatMessageRepository chatMessageRepository;

    private final StudyGroupRepository studyGroupRepository;

    private final GroupMemberRepository groupMemberRepository;

    private final UserRepository userRepository;

    private final ObjectStorageService objectStorageService;

    private final LiveKitProperties liveKitProperties;

    private final CallRoomRepository callRoomRepository;

    @Autowired
    public StudyRoomService(
            ChatMessageRepository chatMessageRepository,
            StudyGroupRepository studyGroupRepository,
            GroupMemberRepository groupMemberRepository,
            UserRepository userRepository,
            ObjectStorageService objectStorageService,
            LiveKitProperties liveKitProperties,
            CallRoomRepository callRoomRepository
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

        this.liveKitProperties =
                liveKitProperties;

        this.callRoomRepository =
                callRoomRepository;
    }

    /*
     * Compatibility constructor for unit tests
     * created before CallRoomRepository existed.
     */
    public StudyRoomService(
            ChatMessageRepository chatMessageRepository,
            StudyGroupRepository studyGroupRepository,
            GroupMemberRepository groupMemberRepository,
            UserRepository userRepository,
            ObjectStorageService objectStorageService,
            LiveKitProperties liveKitProperties
    ) {
        this(
                chatMessageRepository,
                studyGroupRepository,
                groupMemberRepository,
                userRepository,
                objectStorageService,
                liveKitProperties,
                null
        );
    }

    // =========================================================
    // Chat
    // =========================================================

    @Transactional(readOnly = true)
    public Page<ChatMessageResponse> getMessageHistory(
            UUID groupId,
            UUID userId,
            Pageable pageable
    ) {
        ensureGroupExists(groupId);

        ensureMember(
                groupId,
                userId,
                "You must be a member of this study group to access chat"
        );

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
                findGroup(groupId);

        ensureMember(
                groupId,
                userId,
                "You must be a member of this study group to access chat"
        );

        User sender =
                findUser(userId);

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

        ensureMember(
                groupId,
                userId,
                "You must be a member of this study group to access chat"
        );

        User user =
                findUser(userId);

        return new TypingEventResponse(
                groupId,
                user.getId(),
                user.getUsername(),
                user.getDisplayName(),
                typing
        );
    }

    // =========================================================
    // Voice messages
    // =========================================================

    @Transactional
    public ChatMessageResponse sendAudioMessage(
            UUID groupId,
            UUID userId,
            MultipartFile audio,
            Integer durationMs
    ) {
        StudyGroup group =
                findGroup(groupId);

        ensureMember(
                groupId,
                userId,
                "You must be a member of this study group to access chat"
        );

        User sender =
                findUser(userId);

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

        ensureMember(
                groupId,
                userId,
                "You must be a member of this study group to access chat"
        );

        ChatMessage message =
                chatMessageRepository
                        .findById(messageId)
                        .orElseThrow(
                                () -> new NotFoundException(
                                        "Chat message not found"
                                )
                        );

        if (!message.getGroup()
                .getId()
                .equals(groupId)) {

            throw new NotFoundException(
                    "Chat message not found in this study group"
            );
        }

        if (message.getType()
                != ChatMessageType.AUDIO) {

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

        ObjectStorageService.StoredObject storedObject =
                objectStorageService.getObject(
                        message.getMediaUrl()
                );

        return new ChatAudioContent(
                storedObject.content(),
                storedObject.contentType()
        );
    }

    // =========================================================
    // Call rooms
    // =========================================================

    @Transactional
    public CallRoomResponse createCallRoom(
            UUID groupId,
            UUID userId,
            CreateCallRoomRequest request
    ) {
        StudyGroup group =
                findGroup(groupId);

        ensureMember(
                groupId,
                userId,
                "You must be a member of this study group to create a call room"
        );

        User creator =
                findUser(userId);

        String normalizedName =
                request.name() == null
                        ? ""
                        : request.name().trim();

        if (normalizedName.isBlank()) {
            throw new IllegalArgumentException(
                    "Call room name must not be blank"
            );
        }

        if (normalizedName.length() > 100) {
            throw new IllegalArgumentException(
                    "Call room name must not exceed 100 characters"
            );
        }

        CallRoom callRoom =
                new CallRoom(
                        group,
                        normalizedName,
                        creator
                );

        CallRoom saved =
                requireCallRoomRepository()
                        .save(callRoom);

        return toCallRoomResponse(
                saved,
                List.of()
        );
    }

    @Transactional(readOnly = true)
    public List<CallRoomResponse> getCallRooms(
            UUID groupId,
            UUID userId
    ) {
        ensureGroupExists(groupId);

        ensureMember(
                groupId,
                userId,
                "You must be a member of this study group to access call rooms"
        );

        return requireCallRoomRepository()
                .findByGroup_IdOrderByCreatedAtAsc(
                        groupId
                )
                .stream()
                .map(
                        callRoom ->
                                toCallRoomResponse(
                                        callRoom,
                                        getCallRoomParticipants(
                                                groupId,
                                                callRoom.getId()
                                        )
                                )
                )
                .toList();
    }

    @Transactional(readOnly = true)
    public VideoTokenResponse createCallRoomJoinToken(
            UUID groupId,
            UUID callRoomId,
            UUID userId
    ) {
        ensureGroupExists(groupId);

        ensureMember(
                groupId,
                userId,
                "You must be a member of this study group to join a call room"
        );

        CallRoom callRoom =
                requireCallRoomRepository()
                        .findByIdAndGroup_Id(
                                callRoomId,
                                groupId
                        )
                        .orElseThrow(
                                () -> new NotFoundException(
                                        "Call room not found"
                                )
                        );

        User user =
                findUser(userId);

        String roomName =
                buildCallRoomName(
                        groupId,
                        callRoom.getId()
                );

        return createLiveKitJoinToken(
                roomName,
                user
        );
    }

    // =========================================================
    // LiveKit participant presence
    // =========================================================

    private List<CallRoomParticipantResponse> getCallRoomParticipants(
            UUID groupId,
            UUID callRoomId
    ) {
        String roomName =
                buildCallRoomName(
                        groupId,
                        callRoomId
                );

        try {
            RoomServiceClient roomServiceClient =
                    RoomServiceClient.create(
                            liveKitProperties.getInternalUrl(),
                            liveKitProperties.getApiKey(),
                            liveKitProperties.getApiSecret()
                    );

            Response<List<LivekitModels.ParticipantInfo>> response =
                    roomServiceClient
                            .listParticipants(
                                    roomName
                            )
                            .execute();

            if (!response.isSuccessful()) {

                /*
                 * A persisted CallRoom may legitimately have no
                 * active LiveKit room yet because LiveKit creates
                 * the actual room when somebody joins.
                 *
                 * In that case the UI should simply show
                 * zero participants instead of failing the
                 * entire Study Room page.
                 */
                if (response.code() != 404) {
                    log.warn(
                            "Could not retrieve participants for LiveKit room {}. HTTP status: {}",
                            roomName,
                            response.code()
                    );
                }

                return List.of();
            }

            List<LivekitModels.ParticipantInfo> participants =
                    response.body();

            if (participants == null) {
                return List.of();
            }

            return participants
                    .stream()
                    .map(
                            this::toCallRoomParticipantResponse
                    )
                    .toList();

        } catch (IOException | RuntimeException exception) {

            /*
             * Presence is supplementary information.
             * A temporary LiveKit failure must not prevent users
             * from loading their persistent Call Rooms.
             */
            log.warn(
                    "Could not retrieve participants for LiveKit room {}",
                    roomName,
                    exception
            );

            return List.of();
        }
    }

    private CallRoomParticipantResponse toCallRoomParticipantResponse(
            LivekitModels.ParticipantInfo participant
    ) {
        String identity =
                participant.getIdentity();

        String name =
                participant.getName();

        if (name == null
                || name.isBlank()) {

            name = identity;
        }

        return new CallRoomParticipantResponse(
                participant.getSid(),
                identity,
                name
        );
    }

    private String buildCallRoomName(
            UUID groupId,
            UUID callRoomId
    ) {
        return ROOM_PREFIX
                + groupId
                + "-call-"
                + callRoomId;
    }

    // =========================================================
    // LiveKit token creation
    // =========================================================

    private VideoTokenResponse createLiveKitJoinToken(
            String roomName,
            User user
    ) {
        String participantIdentity =
                user.getId().toString();

        String participantName =
                user.getDisplayName();

        if (participantName == null
                || participantName.isBlank()) {

            participantName =
                    user.getUsername();
        }

        AccessToken accessToken =
                new AccessToken(
                        liveKitProperties.getApiKey(),
                        liveKitProperties.getApiSecret()
                );

        accessToken.setIdentity(
                participantIdentity
        );

        accessToken.setName(
                participantName
        );

        accessToken.setTtl(
                TOKEN_TTL_MS
        );

        accessToken.addGrants(
                new RoomJoin(true),
                new RoomName(roomName)
        );

        String token =
                accessToken.toJwt();

        return new VideoTokenResponse(
                liveKitProperties.getPublicUrl(),
                token,
                roomName,
                participantIdentity
        );
    }

    // =========================================================
    // Shared domain helpers
    // =========================================================

    private StudyGroup findGroup(
            UUID groupId
    ) {
        return studyGroupRepository
                .findById(groupId)
                .orElseThrow(
                        () -> new NotFoundException(
                                "Study group not found"
                        )
                );
    }

    private User findUser(
            UUID userId
    ) {
        return userRepository
                .findById(userId)
                .orElseThrow(
                        () -> new NotFoundException(
                                "User not found"
                        )
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
            UUID userId,
            String forbiddenMessage
    ) {
        boolean member =
                groupMemberRepository
                        .existsByGroup_IdAndUser_Id(
                                groupId,
                                userId
                        );

        if (!member) {
            throw new ForbiddenException(
                    forbiddenMessage
            );
        }
    }

    private CallRoomRepository requireCallRoomRepository() {
        if (callRoomRepository == null) {
            throw new IllegalStateException(
                    "CallRoomRepository is required for call room operations"
            );
        }

        return callRoomRepository;
    }

    // =========================================================
    // Response mapping
    // =========================================================

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

    private CallRoomResponse toCallRoomResponse(
            CallRoom callRoom,
            List<CallRoomParticipantResponse> participants
    ) {
        return new CallRoomResponse(
                callRoom.getId(),
                callRoom.getGroup().getId(),
                callRoom.getName(),
                callRoom.getCreatedBy().getId(),
                callRoom.getCreatedBy().getUsername(),
                callRoom.getCreatedAt(),
                participants
        );
    }

    public record ChatAudioContent(
            byte[] content,
            String contentType
    ) {
    }

    // =========================================================
    // Storage transaction cleanup
    // =========================================================

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