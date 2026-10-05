package com.grouplearning.backend.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "study_groups")
public class StudyGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 1000)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GroupVisibility visibility = GroupVisibility.PUBLIC;

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

    protected StudyGroup() {
    }

    /*
     * Backward-compatible constructor.
     *
     * Existing code that creates a group without explicitly
     * specifying visibility will continue to create PUBLIC groups.
     */
    public StudyGroup(
            String name,
            String description,
            User owner
    ) {
        this(
                name,
                description,
                owner,
                GroupVisibility.PUBLIC
        );
    }

    public StudyGroup(
            String name,
            String description,
            User owner,
            GroupVisibility visibility
    ) {
        this.name = name;
        this.description = description;
        this.owner = owner;
        this.visibility =
                visibility == null
                        ? GroupVisibility.PUBLIC
                        : visibility;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public User getOwner() {
        return owner;
    }

    public GroupVisibility getVisibility() {
        return visibility;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}