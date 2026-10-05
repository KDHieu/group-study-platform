package com.grouplearning.backend.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "group_join_requests",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_group_join_requests_group_user",
                        columnNames = {
                                "group_id",
                                "user_id"
                        }
                )
        }
)
public class GroupJoinRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "group_id",
            nullable = false
    )
    private StudyGroup group;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private User requester;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 20
    )
    private GroupJoinRequestStatus status;

    @CreationTimestamp
    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    @UpdateTimestamp
    @Column(
            name = "updated_at",
            nullable = false
    )
    private Instant updatedAt;

    protected GroupJoinRequest() {
    }

    public GroupJoinRequest(
            StudyGroup group,
            User requester
    ) {
        this.group = group;
        this.requester = requester;
        this.status =
                GroupJoinRequestStatus.PENDING;
    }

    public UUID getId() {
        return id;
    }

    public StudyGroup getGroup() {
        return group;
    }

    public User getRequester() {
        return requester;
    }

    public GroupJoinRequestStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void approve() {
        this.status =
                GroupJoinRequestStatus.APPROVED;
    }

    public void reject() {
        this.status =
                GroupJoinRequestStatus.REJECTED;
    }

    public void reopen() {
        this.status =
                GroupJoinRequestStatus.PENDING;
    }
}