import { apiClient } from "@/api/client"
import type {
    ChatHistoryPage,
    ChatMessage,
} from "@/types/chat"

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

    async uploadAudio(
        groupId: string,
        audio: Blob,
        durationMs: number,
    ): Promise<ChatMessage> {
        const formData =
            new FormData()

        const extension =
            audio.type.includes("ogg")
                ? "ogg"
                : audio.type.includes("mp4")
                    ? "m4a"
                    : "webm"

        const file =
            new File(
                [audio],
                `voice-message-${Date.now()}.${extension}`,
                {
                    type:
                        audio.type ||
                        "audio/webm",
                },
            )

        formData.append(
            "audio",
            file,
        )

        formData.append(
            "durationMs",
            String(durationMs),
        )

        const response =
            await apiClient.post<ChatMessage>(
                `/groups/${groupId}/messages/audio`,
                formData,
            )

        return response.data
    },

    async getAudioBlob(
        mediaUrl: string,
    ): Promise<Blob> {
        const path =
            mediaUrl.startsWith("/api/")
                ? mediaUrl.substring(4)
                : mediaUrl

        const response =
            await apiClient.get<Blob>(
                path,
                {
                    responseType:
                        "blob",
                },
            )

        return response.data
    },
}