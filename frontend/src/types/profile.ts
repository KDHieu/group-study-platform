export interface UserProfile {
    id: string;
    username: string;
    displayName: string;
    bio: string | null;
    avatarUrl: string | null;
}

export interface UpdateProfileRequest {
    displayName: string;
    bio: string | null;
}