import { apiClient } from "@/api/client"

export interface VideoTokenResponse {
    serverUrl: string
    token: string
    roomName: string
    participantIdentity: string
}

export const videoApi = {
    async createJoinToken(
        groupId: string,
    ): Promise<VideoTokenResponse> {
        const response =
            await apiClient.post<VideoTokenResponse>(
                `/groups/${groupId}/video/token`,
            )

        return response.data
    },
}