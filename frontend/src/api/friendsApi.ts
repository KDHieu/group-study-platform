import { apiClient } from "@/api/client";

import type {
    Friend,
    FriendRequest,
    PageResponse,
    UserSearchResult,
} from "@/types/friend";

export async function searchUsers(
    query: string,
    page = 0,
    size = 10,
): Promise<PageResponse<UserSearchResult>> {
    const response =
        await apiClient.get<
            PageResponse<UserSearchResult>
        >(
            "/users/search",
            {
                params: {
                    query,
                    page,
                    size,
                },
            },
        );

    return response.data;
}

export async function getFriends(): Promise<Friend[]> {
    const response =
        await apiClient.get<Friend[]>(
            "/friends",
        );

    return response.data;
}

export async function getIncomingRequests():
    Promise<FriendRequest[]> {
    const response =
        await apiClient.get<FriendRequest[]>(
            "/friends/requests/incoming",
        );

    return response.data;
}

export async function getOutgoingRequests():
    Promise<FriendRequest[]> {
    const response =
        await apiClient.get<FriendRequest[]>(
            "/friends/requests/outgoing",
        );

    return response.data;
}

export async function sendFriendRequest(
    userId: string,
): Promise<FriendRequest> {
    const response =
        await apiClient.post<FriendRequest>(
            `/friends/requests/${userId}`,
        );

    return response.data;
}

export async function acceptFriendRequest(
    requestId: string,
): Promise<FriendRequest> {
    const response =
        await apiClient.post<FriendRequest>(
            `/friends/requests/${requestId}/accept`,
        );

    return response.data;
}

export async function rejectFriendRequest(
    requestId: string,
): Promise<void> {
    await apiClient.post(
        `/friends/requests/${requestId}/reject`,
    );
}

export async function cancelFriendRequest(
    requestId: string,
): Promise<void> {
    await apiClient.delete(
        `/friends/requests/${requestId}`,
    );
}

export async function removeFriend(
    friendUserId: string,
): Promise<void> {
    await apiClient.delete(
        `/friends/${friendUserId}`,
    );
}