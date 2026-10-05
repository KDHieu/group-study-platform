import { apiClient } from "@/api/client"
import type {
    CreateGroupRequest,
    GroupMember,
    GroupPageResponse,
    StudyGroup,
} from "@/types/group"
import type {
    GroupJoinRequest,
} from "@/types/groupJoinRequest"

export async function getGroups(
    search = "",
    page = 0,
    size = 10,
) {
    const response =
        await apiClient.get<GroupPageResponse>(
            "/groups",
            {
                params: {
                    search,
                    page,
                    size,
                },
            },
        )

    return response.data
}

export async function getMyGroups(
    search = "",
    page = 0,
    size = 10,
) {
    const response =
        await apiClient.get<GroupPageResponse>(
            "/groups/mine",
            {
                params: {
                    search,
                    page,
                    size,
                },
            },
        )

    return response.data
}

export async function getGroupById(
    groupId: string,
) {
    const response =
        await apiClient.get<StudyGroup>(
            `/groups/${groupId}`,
        )

    return response.data
}

export async function createGroup(
    request: CreateGroupRequest,
) {
    const response =
        await apiClient.post<StudyGroup>(
            "/groups",
            request,
        )

    return response.data
}

export async function getGroupMembers(
    groupId: string,
) {
    const response =
        await apiClient.get<GroupMember[]>(
            `/groups/${groupId}/members`,
        )

    return response.data
}

export async function joinGroup(
    groupId: string,
) {
    await apiClient.post(
        `/groups/${groupId}/join`,
    )
}

export async function getMyJoinRequest(
    groupId: string,
) {
    const response =
        await apiClient.get<GroupJoinRequest>(
            `/groups/${groupId}/join-request/me`,
        )

    if (response.status === 204) {
        return null
    }

    return response.data
}

export async function getPendingJoinRequests(
    groupId: string,
) {
    const response =
        await apiClient.get<GroupJoinRequest[]>(
            `/groups/${groupId}/join-requests`,
        )

    return response.data
}

export async function approveJoinRequest(
    groupId: string,
    requestId: string,
) {
    await apiClient.post(
        `/groups/${groupId}/join-requests/${requestId}/approve`,
    )
}

export async function rejectJoinRequest(
    groupId: string,
    requestId: string,
) {
    await apiClient.post(
        `/groups/${groupId}/join-requests/${requestId}/reject`,
    )
}

export async function leaveGroup(
    groupId: string,
) {
    await apiClient.delete(
        `/groups/${groupId}/members/me`,
    )
}

export async function deleteGroup(
    groupId: string,
) {
    await apiClient.delete(
        `/groups/${groupId}`,
    )
}