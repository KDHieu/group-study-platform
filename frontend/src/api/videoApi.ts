import { apiClient } from "@/api/client"
import type {
    CallRoom,
    CreateCallRoomRequest,
} from "@/types/callRoom"

export interface VideoTokenResponse {
    serverUrl: string
    token: string
    roomName: string
    participantIdentity: string
}

export const videoApi = {
    async getCallRooms(
        groupId: string,
    ): Promise<CallRoom[]> {
        const response =
            await apiClient.get<CallRoom[]>(
                `/groups/${groupId}/call-rooms`,
            )

        return response.data
    },

    async createCallRoom(
        groupId: string,
        request: CreateCallRoomRequest,
    ): Promise<CallRoom> {
        const response =
            await apiClient.post<CallRoom>(
                `/groups/${groupId}/call-rooms`,
                request,
            )

        return response.data
    },

    async createCallRoomJoinToken(
        groupId: string,
        callRoomId: string,
    ): Promise<VideoTokenResponse> {
        const response =
            await apiClient.post<VideoTokenResponse>(
                `/groups/${groupId}/call-rooms/${callRoomId}/token`,
            )

        return response.data
    },
}