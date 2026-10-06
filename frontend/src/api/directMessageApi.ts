import {apiClient} from "@/api/client.ts";

import type {
    DirectConversation,
    DirectMessage,
    DirectMessagePageResponse,
    SendDirectMessageRequest,
} from "@/types/directMessage.ts"

export const directMessageApi = {
    async getConversations(): Promise<
        DirectConversation[]
    > {
        const response =
            await apiClient.get<
                DirectConversation[]
            >(
                "/messages/conversations",
            )

        return response.data
    },

    async getConversation(
        userId: string,
        page = 0,
        size = 30,
    ): Promise<DirectMessagePageResponse> {
        const response =
            await apiClient.get<
                DirectMessagePageResponse
            >(
                `/messages/${userId}`,
                {
                    params: {
                        page,
                        size,
                    },
                },
            )

        return response.data
    },

    async sendMessage(
        userId: string,
        request: SendDirectMessageRequest,
    ): Promise<DirectMessage> {
        const response =
            await apiClient.post<DirectMessage>(
                `/messages/${userId}`,
                request,
            )

        return response.data
    },
}