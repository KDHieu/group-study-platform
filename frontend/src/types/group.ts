export type GroupVisibility =
    | "PUBLIC"
    | "PRIVATE"

export interface StudyGroup {
    id: string
    name: string
    description: string | null
    ownerId: string
    ownerUsername: string
    visibility: GroupVisibility
    createdAt: string
}

export interface PageInfo {
    size: number
    number: number
    totalElements: number
    totalPages: number
}

export interface GroupPageResponse {
    content: StudyGroup[]
    page: PageInfo
}

export interface CreateGroupRequest {
    name: string
    description?: string
    visibility: GroupVisibility
}

export type GroupMemberRole =
    | "OWNER"
    | "MEMBER"

export interface GroupMember {
    userId: string
    username: string
    role: GroupMemberRole
    joinedAt: string
}