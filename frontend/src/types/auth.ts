export interface User {
    id: string;
    username: string;
    email: string;
    createdAt: string;
}

export interface LoginRequest {
    email: string;
    password: string;
}

export interface LoginResponse {
    accessToken: string;
    tokenType: string;
    expiresIn: number;
    user: User;
}

export interface RegisterRequest {
    username: string;
    email: string;
    password: string;
}

export interface CurrentUserResponse {
    id: string;
    username: string;
}