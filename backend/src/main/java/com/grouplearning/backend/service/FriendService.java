package com.grouplearning.backend.service;

import com.grouplearning.backend.dto.response.FriendResponse;
import com.grouplearning.backend.entity.FriendshipStatus;
import com.grouplearning.backend.exception.ForbiddenException;
import com.grouplearning.backend.dto.response.FriendRequestResponse;
import com.grouplearning.backend.dto.response.FriendUserResponse;
import com.grouplearning.backend.entity.FriendRelationship;
import com.grouplearning.backend.entity.User;
import com.grouplearning.backend.exception.ConflictException;
import com.grouplearning.backend.exception.NotFoundException;
import com.grouplearning.backend.repository.FriendRelationshipRepository;
import com.grouplearning.backend.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.List;

@Service
public class FriendService {

    private final FriendRelationshipRepository
            friendRelationshipRepository;

    private final UserRepository userRepository;

    public FriendService(
            FriendRelationshipRepository
                    friendRelationshipRepository,
            UserRepository userRepository
    ) {
        this.friendRelationshipRepository =
                friendRelationshipRepository;

        this.userRepository =
                userRepository;
    }

    @Transactional
    public FriendRequestResponse sendRequest(
            UUID requesterId,
            UUID addresseeId
    ) {
        if (requesterId.equals(addresseeId)) {
            throw new ConflictException(
                    "You cannot send a friend request to yourself"
            );
        }

        User requester =
                findUser(requesterId);

        User addressee =
                findUser(addresseeId);

        /*
         * Friendly application-level check.
         *
         * This gives a clean error in the normal case,
         * but it is NOT our concurrency guarantee.
         */
        if (friendRelationshipRepository
                .existsBetweenUsers(
                        requesterId,
                        addresseeId
                )) {

            throw new ConflictException(
                    "A friend relationship already exists between these users"
            );
        }

        FriendRelationship relationship =
                new FriendRelationship(
                        requester,
                        addressee
                );

        try {

            FriendRelationship saved =
                    friendRelationshipRepository
                            .saveAndFlush(
                                    relationship
                            );

            return toRequestResponse(saved);

        } catch (
                DataIntegrityViolationException exception
        ) {

            throw new ConflictException(
                    "A friend relationship already exists between these users"
            );
        }
    }

    private User findUser(UUID userId) {
        return userRepository
                .findById(userId)
                .orElseThrow(
                        () -> new NotFoundException(
                                "User not found"
                        )
                );
    }

    private FriendRequestResponse toRequestResponse(
            FriendRelationship relationship
    ) {
        return new FriendRequestResponse(
                relationship.getId(),
                toFriendUser(
                        relationship.getRequester()
                ),
                toFriendUser(
                        relationship.getAddressee()
                ),
                relationship.getStatus(),
                relationship.getCreatedAt()
        );
    }

    private FriendUserResponse toFriendUser(
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

        return new FriendUserResponse(
                user.getId(),
                user.getUsername(),
                user.getDisplayName(),
                avatarUrl
        );
    }

    @Transactional(readOnly = true)
    public List<FriendRequestResponse> getIncomingRequests(
            UUID userId
    ) {
        return friendRelationshipRepository
                .findIncomingRequests(
                        userId,
                        FriendshipStatus.PENDING
                )
                .stream()
                .map(this::toRequestResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FriendRequestResponse> getOutgoingRequests(
            UUID userId
    ) {
        return friendRelationshipRepository
                .findOutgoingRequests(
                        userId,
                        FriendshipStatus.PENDING
                )
                .stream()
                .map(this::toRequestResponse)
                .toList();
    }

    @Transactional
    public FriendRequestResponse acceptRequest(
            UUID userId,
            UUID requestId
    ) {
        FriendRelationship relationship =
                findRelationship(requestId);

        if (!relationship.isAddressee(userId)) {
            throw new ForbiddenException(
                    "Only the receiver can accept this friend request"
            );
        }

        if (relationship.getStatus()
                != FriendshipStatus.PENDING) {
            throw new ConflictException(
                    "Only pending friend requests can be accepted"
            );
        }

        relationship.accept();

        return toRequestResponse(relationship);
    }

    @Transactional
    public void rejectRequest(
            UUID userId,
            UUID requestId
    ) {
        FriendRelationship relationship =
                findRelationship(requestId);

        if (!relationship.isAddressee(userId)) {
            throw new ForbiddenException(
                    "Only the receiver can reject this friend request"
            );
        }

        if (relationship.getStatus()
                != FriendshipStatus.PENDING) {
            throw new ConflictException(
                    "Only pending friend requests can be rejected"
            );
        }

        friendRelationshipRepository.delete(
                relationship
        );
    }

    @Transactional
    public void cancelRequest(
            UUID userId,
            UUID requestId
    ) {
        FriendRelationship relationship =
                findRelationship(requestId);

        if (!relationship.isRequester(userId)) {
            throw new ForbiddenException(
                    "Only the sender can cancel this friend request"
            );
        }

        if (relationship.getStatus()
                != FriendshipStatus.PENDING) {
            throw new ConflictException(
                    "Only pending friend requests can be cancelled"
            );
        }

        friendRelationshipRepository.delete(
                relationship
        );
    }

    private FriendRelationship findRelationship(
            UUID relationshipId
    ) {
        return friendRelationshipRepository
                .findDetailedById(
                        relationshipId
                )
                .orElseThrow(
                        () -> new NotFoundException(
                                "Friend request not found"
                        )
                );
    }

    @Transactional(readOnly = true)
    public List<FriendResponse> getFriends(
            UUID userId
    ) {
        return friendRelationshipRepository
                .findByUserAndStatus(
                        userId,
                        FriendshipStatus.ACCEPTED
                )
                .stream()
                .map(relationship -> {
                    User friend =
                            relationship.getOtherUser(
                                    userId
                            );

                    return new FriendResponse(
                            relationship.getId(),
                            toFriendUser(friend),
                            relationship.getUpdatedAt()
                    );
                })
                .toList();
    }

    @Transactional
    public void removeFriend(
            UUID userId,
            UUID friendUserId
    ) {
        if (userId.equals(friendUserId)) {
            throw new NotFoundException(
                    "Friendship not found"
            );
        }

        FriendRelationship relationship =
                friendRelationshipRepository
                        .findBetweenUsersByStatus(
                                userId,
                                friendUserId,
                                FriendshipStatus.ACCEPTED
                        )
                        .orElseThrow(
                                () -> new NotFoundException(
                                        "Friendship not found"
                                )
                        );

        friendRelationshipRepository.delete(
                relationship
        );
    }
}