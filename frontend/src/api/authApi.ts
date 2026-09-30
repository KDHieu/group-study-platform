import { apiClient } from "@/api/client";
import type {
    CurrentUserResponse,
    LoginRequest,
    LoginResponse,
    RegisterRequest,
    User,
} from "@/types/auth";

export const authApi = {
    async login(request: LoginRequest): Promise<LoginResponse> {
        const response = await apiClient.post<LoginResponse>(
            "/auth/login",
            request,
        );

        return response.data;
    },

    async register(request: RegisterRequest): Promise<User> {
        const response = await apiClient.post<User>(
            "/auth/register",
            request,
        );

        return response.data;
    },

    async getCurrentUser(): Promise<CurrentUserResponse> {
        const response =
            await apiClient.get<CurrentUserResponse>("/auth/me");

        return response.data;
    },
};