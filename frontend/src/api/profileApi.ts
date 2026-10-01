import { apiClient } from "@/api/client";
import type {
    UpdateProfileRequest,
    UserProfile,
} from "@/types/profile";

export async function getMyProfile(): Promise<UserProfile> {
    const response =
        await apiClient.get<UserProfile>(
            "/users/me/profile",
        );

    return response.data;
}

export async function getUserProfile(
    userId: string,
): Promise<UserProfile> {
    const response =
        await apiClient.get<UserProfile>(
            `/users/${userId}/profile`,
        );

    return response.data;
}

export async function updateMyProfile(
    request: UpdateProfileRequest,
): Promise<UserProfile> {
    const response =
        await apiClient.patch<UserProfile>(
            "/users/me/profile",
            request,
        );

    return response.data;
}

export async function updateMyAvatar(
    file: File,
): Promise<UserProfile> {
    const formData = new FormData();

    formData.append("file", file);

    const response =
        await apiClient.patch<UserProfile>(
            "/users/me/avatar",
            formData,
        );

    return response.data;
}

export function resolveAvatarUrl(
    avatarUrl: string | null,
): string | undefined {
    if (!avatarUrl) {
        return undefined;
    }

    if (
        avatarUrl.startsWith("http://") ||
        avatarUrl.startsWith("https://")
    ) {
        return avatarUrl;
    }

    const apiBaseUrl =
        import.meta.env.VITE_API_BASE_URL ??
        "http://localhost:8080/api";

    const apiOrigin =
        apiBaseUrl.replace(/\/api\/?$/, "");

    return `${apiOrigin}${avatarUrl}`;
}