package com.grouplearning.backend.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "friend_relationships")
public class FriendRelationship {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "requester_id",
            nullable = false
    )
    private User requester;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "addressee_id",
            nullable = false
    )
    private User addressee;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 20
    )
    private FriendshipStatus status;

    @Column(
            name = "created_at",
            nullable = false
    )
    private Instant createdAt;

    @Column(
            name = "updated_at",
            nullable = false
    )
    private Instant updatedAt;

    protected FriendRelationship() {
    }

    public FriendRelationship(
            User requester,
            User addressee
    ) {
        this.id = UUID.randomUUID();

        this.requester = requester;
        this.addressee = addressee;

        this.status =
                FriendshipStatus.PENDING;

        Instant now = Instant.now();

        this.createdAt = now;
        this.updatedAt = now;
    }

    public void accept() {
        this.status =
                FriendshipStatus.ACCEPTED;

        this.updatedAt =
                Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public User getRequester() {
        return requester;
    }

    public User getAddressee() {
        return addressee;
    }

    public FriendshipStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public boolean involves(UUID userId) {
        return requester.getId().equals(userId)
                || addressee.getId().equals(userId);
    }

    public boolean isRequester(UUID userId) {
        return requester.getId().equals(userId);
    }

    public boolean isAddressee(UUID userId) {
        return addressee.getId().equals(userId);
    }

    public User getOtherUser(UUID userId) {
        if (requester.getId().equals(userId)) {
            return addressee;
        }

        if (addressee.getId().equals(userId)) {
            return requester;
        }

        throw new IllegalArgumentException(
                "User is not part of this friendship"
        );
    }
}