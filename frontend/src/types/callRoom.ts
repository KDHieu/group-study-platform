export interface CallRoomParticipant {
    sid: string
    identity: string
    name: string
}

export interface CallRoom {
    id: string
    groupId: string
    name: string
    createdById: string
    createdByUsername: string
    createdAt: string
    participants: CallRoomParticipant[]
}

export interface CreateCallRoomRequest {
    name: string
}