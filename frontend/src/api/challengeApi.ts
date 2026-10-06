import { apiClient } from "@/api/client"
import type {
    ChallengeHistoryPage,
    ChallengeStreak,
    TodayChallenge,
} from "@/types/challenge"

export const challengeApi = {
    async getToday(): Promise<TodayChallenge> {
        const response =
            await apiClient.get<TodayChallenge>(
                "/challenges/today",
            )

        return response.data
    },

    async submitToday(
        selectedOption: string,
    ): Promise<TodayChallenge> {
        const response =
            await apiClient.post<TodayChallenge>(
                "/challenges/today/submit",
                {
                    selectedOption,
                },
            )

        return response.data
    },

    async getHistory(
        page = 0,
        size = 10,
    ): Promise<ChallengeHistoryPage> {
        const response =
            await apiClient.get<ChallengeHistoryPage>(
                "/challenges/history",
                {
                    params: {
                        page,
                        size,
                    },
                },
            )

        return response.data
    },

    async getStreak(): Promise<ChallengeStreak> {
        const response =
            await apiClient.get<ChallengeStreak>(
                "/challenges/streak",
            )

        return response.data
    },
}