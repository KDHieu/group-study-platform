import axios from "axios"

interface ApiErrorResponse {
    message?: string
}

export function getApiErrorMessage(
    error: unknown,
    fallback: string,
) {
    if (
        axios.isAxiosError<ApiErrorResponse>(error) &&
        error.response?.data?.message
    ) {
        return error.response.data.message
    }

    return fallback
}