import { apiClient } from "@/api/client"
import type { ChatHistoryPage } from "@/types/chat"

export const chatApi = {
    async getMessageHistory(
        groupId: string,
        page = 0,
        size = 30,
    ): Promise<ChatHistoryPage> {
        const response =
            await apiClient.get<ChatHistoryPage>(
                `/groups/${groupId}/messages`,
                {
                    params: {
                        page,
                        size,
                        sort: "createdAt,desc",
                    },
                },
            )

        return response.data
    },
}