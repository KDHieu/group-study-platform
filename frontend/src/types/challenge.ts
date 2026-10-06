export interface ChallengeOption {
    key: string
    text: string
}

export interface TodayChallenge {
    id: string
    challengeDate: string
    question: string
    options: ChallengeOption[]

    completed: boolean

    selectedOption: string | null
    correct: boolean | null

    correctOption: string | null
    explanation: string | null

    completedAt: string | null
}

export interface ChallengeHistoryItem {
    challengeId: string
    challengeDate: string
    question: string

    selectedOption: string
    correctOption: string

    correct: boolean

    explanation: string
    completedAt: string
}

export interface ChallengeStreak {
    currentStreak: number
    longestStreak: number
    completedChallenges: number
}

export interface ChallengeHistoryPage {
    content: ChallengeHistoryItem[]

    page: {
        size: number
        number: number
        totalElements: number
        totalPages: number
    }
}