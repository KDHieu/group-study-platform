export interface DirectMessage {
    id: string
    senderId: string
    receiverId: string
    content: string
    createdAt: string
}

export interface DirectConversation {
    userId: string
    username: string
    displayName: string | null
    avatarUrl: string | null
    lastMessage: DirectMessage | null
}

export interface DirectMessagePageResponse {
    content: DirectMessage[]
    page: {
        size: number
        number: number
        totalElements: number
        totalPages: number
    }
}

export interface SendDirectMessageRequest {
    content: string
}