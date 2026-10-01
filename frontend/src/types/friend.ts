export interface FriendUser {
    id: string;
    username: string;
    displayName: string;
    avatarUrl: string | null;
}

export type FriendshipStatus =
    | "PENDING"
    | "ACCEPTED";

export interface FriendRequest {
    id: string;
    requester: FriendUser;
    addressee: FriendUser;
    status: FriendshipStatus;
    createdAt: string;
}

export interface Friend {
    relationshipId: string;
    friend: FriendUser;
    friendsSince: string;
}

export interface UserSearchResult {
    id: string;
    username: string;
    displayName: string;
    bio: string | null;
    avatarUrl: string | null;
}

export interface PageMetadata {
    size: number;
    number: number;
    totalElements: number;
    totalPages: number;
}

export interface PageResponse<T> {
    content: T[];
    page: PageMetadata;
}