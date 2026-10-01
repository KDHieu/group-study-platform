import axios from "axios";

import { tokenStorage } from "@/auth/tokenStorage";

export const apiClient = axios.create({
    baseURL:
        import.meta.env.VITE_API_BASE_URL ??
        "http://localhost:8080/api",
});

apiClient.interceptors.request.use((config) => {
    const token = tokenStorage.get();

    if (token) {
        config.headers.Authorization =
            `Bearer ${token}`;
    }

    return config;
});

apiClient.interceptors.response.use(
    (response) => response,

    (error) => {
        if (
            axios.isAxiosError(error) &&
            error.response?.status === 401
        ) {
            const requestUrl =
                error.config?.url ?? "";

            const isPublicAuthRequest =
                requestUrl.includes("/auth/login") ||
                requestUrl.includes("/auth/register");

            if (!isPublicAuthRequest) {
                tokenStorage.remove();

                window.dispatchEvent(
                    new Event("auth:unauthorized"),
                );
            }
        }

        return Promise.reject(error);
    },
);