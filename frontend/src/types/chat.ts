export type ChatMessageType =
    | "TEXT"
    | "AUDIO"
    | "IMAGE"
    | "FILE"

export interface ChatSender {
    id: string
    username: string
    displayName: string
    avatarUrl: string | null
}

export interface ChatMessage {
    id: string
    groupId: string
    sender: ChatSender
    type: ChatMessageType
    content: string | null
    mediaUrl: string | null
    createdAt: string
}

export interface TypingEvent {
    groupId: string
    userId: string
    username: string
    displayName: string
    typing: boolean
}

export interface ChatPageMetadata {
    size: number
    number: number
    totalElements: number
    totalPages: number
}

export interface ChatHistoryPage {
    content: ChatMessage[]
    page: ChatPageMetadata
}