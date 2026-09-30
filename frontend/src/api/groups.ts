import {apiClient} from "@/api/client"
import type {
    CreateGroupRequest,
    GroupMember,
    GroupPageResponse,
    StudyGroup,
} from "@/types/group"

export async function getGroups(
    search = "",
    page = 0,
    size = 10,
) {
    const response = await apiClient.get<GroupPageResponse>(
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

export async function getGroupById(
    groupId: string,
) {
    const response = await apiClient.get<StudyGroup>(
        `/groups/${groupId}`,
    )

    return response.data
}

export async function createGroup(
    request: CreateGroupRequest,
) {
    const response = await apiClient.post<StudyGroup>(
        "/groups",
        request,
    )

    return response.data
}

export async function getGroupMembers(
    groupId: string,
) {
    const response = await apiClient.get<GroupMember[]>(
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