package com.grouplearning.backend.service;

import com.grouplearning.backend.dto.request.SendDirectMessageRequest;
import com.grouplearning.backend.dto.response.DirectConversationResponse;
import com.grouplearning.backend.dto.response.DirectMessageResponse;
import com.grouplearning.backend.entity.DirectMessage;
import com.grouplearning.backend.entity.FriendRelationship;
import com.grouplearning.backend.entity.FriendshipStatus;
import com.grouplearning.backend.entity.User;
import com.grouplearning.backend.exception.ForbiddenException;
import com.grouplearning.backend.exception.NotFoundException;
import com.grouplearning.backend.repository.DirectMessageRepository;
import com.grouplearning.backend.repository.FriendRelationshipRepository;
import com.grouplearning.backend.repository.UserRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class DirectMessageService {

    private final DirectMessageRepository directMessageRepository;

    private final FriendRelationshipRepository friendRelationshipRepository;

    private final UserRepository userRepository;

    public DirectMessageService(
            DirectMessageRepository directMessageRepository,
            FriendRelationshipRepository friendRelationshipRepository,
            UserRepository userRepository
    ) {
        this.directMessageRepository =
                directMessageRepository;

        this.friendRelationshipRepository =
                friendRelationshipRepository;

        this.userRepository =
                userRepository;
    }

    @Transactional(readOnly = true)
    public List<DirectConversationResponse> getConversations(
            UUID userId
    ) {
        findUser(userId);

        List<FriendRelationship> friendships =
                friendRelationshipRepository
                        .findByUserAndStatus(
                                userId,
                                FriendshipStatus.ACCEPTED
                        );

        return friendships
                .stream()
                .map(
                        friendship ->
                                toConversationCandidate(
                                        userId,
                                        friendship
                                )
                )
                .sorted(
                        Comparator
                                .comparing(
                                        ConversationCandidate::sortAt
                                )
                                .reversed()
                )
                .map(
                        ConversationCandidate::response
                )
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<DirectMessageResponse> getConversation(
            UUID userId,
            UUID otherUserId,
            Pageable pageable
    ) {
        findUser(userId);
        findUser(otherUserId);

        ensureAcceptedFriends(
                userId,
                otherUserId
        );

        return directMessageRepository
                .findConversation(
                        userId,
                        otherUserId,
                        pageable
                )
                .map(this::toResponse);
    }

    @Transactional
    public DirectMessageResponse sendMessage(
            UUID senderId,
            UUID receiverId,
            SendDirectMessageRequest request
    ) {
        if (senderId.equals(receiverId)) {
            throw new IllegalArgumentException(
                    "You cannot send a direct message to yourself"
            );
        }

        User sender =
                findUser(senderId);

        User receiver =
                findUser(receiverId);

        ensureAcceptedFriends(
                senderId,
                receiverId
        );

        String content =
                request.content() == null
                        ? ""
                        : request.content().trim();

        if (content.isBlank()) {
            throw new IllegalArgumentException(
                    "Message content must not be blank"
            );
        }

        if (content.length() > 2000) {
            throw new IllegalArgumentException(
                    "Message content must not exceed 2000 characters"
            );
        }

        DirectMessage message =
                new DirectMessage(
                        sender,
                        receiver,
                        content
                );

        DirectMessage saved =
                directMessageRepository.save(
                        message
                );

        return toResponse(saved);
    }

    private ConversationCandidate toConversationCandidate(
            UUID userId,
            FriendRelationship friendship
    ) {
        User otherUser;

        if (friendship
                .getRequester()
                .getId()
                .equals(userId)) {

            otherUser =
                    friendship.getAddressee();

        } else {
            otherUser =
                    friendship.getRequester();
        }

        List<DirectMessage> latestMessages =
                directMessageRepository
                        .findLatestBetweenUsers(
                                userId,
                                otherUser.getId(),
                                PageRequest.of(
                                        0,
                                        1
                                )
                        );

        DirectMessageResponse lastMessage =
                latestMessages.isEmpty()
                        ? null
                        : toResponse(
                        latestMessages.get(0)
                );

        Instant sortAt =
                lastMessage != null
                        ? lastMessage.createdAt()
                        : friendship.getUpdatedAt();

        DirectConversationResponse response =
                new DirectConversationResponse(
                        otherUser.getId(),
                        otherUser.getUsername(),
                        otherUser.getDisplayName(),
                        resolveAvatarUrl(
                                otherUser
                        ),
                        lastMessage
                );

        return new ConversationCandidate(
                response,
                sortAt
        );
    }

    private void ensureAcceptedFriends(
            UUID userId,
            UUID otherUserId
    ) {
        boolean acceptedFriendship =
                friendRelationshipRepository
                        .findByUserAndStatus(
                                userId,
                                FriendshipStatus.ACCEPTED
                        )
                        .stream()
                        .anyMatch(
                                friendship ->
                                        friendship
                                                .involves(
                                                        otherUserId
                                                )
                        );

        if (!acceptedFriendship) {
            throw new ForbiddenException(
                    "You can only message accepted friends"
            );
        }
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

    private DirectMessageResponse toResponse(
            DirectMessage message
    ) {
        return new DirectMessageResponse(
                message.getId(),
                message
                        .getSender()
                        .getId(),
                message
                        .getReceiver()
                        .getId(),
                message.getContent(),
                message.getCreatedAt()
        );
    }

    private String resolveAvatarUrl(
            User user
    ) {
        if (user.getAvatarUrl() == null
                || user.getAvatarUrl().isBlank()) {

            return null;
        }

        return "/api/users/"
                + user.getId()
                + "/avatar";
    }

    private record ConversationCandidate(
            DirectConversationResponse response,
            Instant sortAt
    ) {
    }
}