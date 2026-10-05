export type GroupJoinRequestStatus =
    | "PENDING"
    | "APPROVED"
    | "REJECTED"

export interface GroupJoinRequest {
    id: string
    groupId: string
    userId: string
    username: string
    status: GroupJoinRequestStatus
    createdAt: string
    updatedAt: string
}