package com.grouplearning.backend.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "chat_messages",
        indexes = {
                @Index(
                        name = "idx_chat_messages_group_created_at",
                        columnList = "group_id, created_at"
                )
        }
)
public class ChatMessage {

    @Id
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
            name = "sender_id",
            nullable = false
    )
    private User sender;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 20
    )
    private ChatMessageType type;

    @Column(
            columnDefinition = "TEXT"
    )
    private String content;

    @Column(
            name = "media_url",
            length = 1000
    )
    private String mediaUrl;

    @Column(
            name = "created_at",
            nullable = false
    )
    private Instant createdAt;

    protected ChatMessage() {
    }

    public ChatMessage(
            StudyGroup group,
            User sender,
            ChatMessageType type,
            String content,
            String mediaUrl
    ) {
        this.id = UUID.randomUUID();
        this.group = group;
        this.sender = sender;
        this.type = type;
        this.content = content;
        this.mediaUrl = mediaUrl;
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public StudyGroup getGroup() {
        return group;
    }

    public User getSender() {
        return sender;
    }

    public ChatMessageType getType() {
        return type;
    }

    public String getContent() {
        return content;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}