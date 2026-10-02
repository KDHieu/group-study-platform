package com.grouplearning.backend.service;

import com.grouplearning.backend.config.LiveKitProperties;
import com.grouplearning.backend.dto.response.VideoTokenResponse;
import com.grouplearning.backend.entity.User;
import com.grouplearning.backend.exception.ForbiddenException;
import com.grouplearning.backend.exception.NotFoundException;
import com.grouplearning.backend.repository.GroupMemberRepository;
import com.grouplearning.backend.repository.StudyGroupRepository;
import com.grouplearning.backend.repository.UserRepository;

import io.livekit.server.AccessToken;
import io.livekit.server.RoomJoin;
import io.livekit.server.RoomName;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class LiveKitTokenService {

    private static final long TOKEN_TTL_MS =
            15 * 60 * 1000L;

    private static final String ROOM_PREFIX =
            "study-group-";

    private final LiveKitProperties properties;

    private final StudyGroupRepository studyGroupRepository;

    private final GroupMemberRepository groupMemberRepository;

    private final UserRepository userRepository;

    public LiveKitTokenService(
            LiveKitProperties properties,
            StudyGroupRepository studyGroupRepository,
            GroupMemberRepository groupMemberRepository,
            UserRepository userRepository
    ) {
        this.properties =
                properties;

        this.studyGroupRepository =
                studyGroupRepository;

        this.groupMemberRepository =
                groupMemberRepository;

        this.userRepository =
                userRepository;
    }

    @Transactional(readOnly = true)
    public VideoTokenResponse createJoinToken(
            UUID groupId,
            UUID userId
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

        String roomName =
                ROOM_PREFIX + groupId;

        /*
         * A user's UUID is stable and unique,
         * therefore it is suitable as the
         * LiveKit participant identity.
         */
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
                        properties.getApiKey(),
                        properties.getApiSecret()
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
                properties.getPublicUrl(),
                token,
                roomName,
                participantIdentity
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
                    "You must be a member of this study group to join the video room"
            );
        }
    }
}