import { createContext } from "react";

import type {
    CurrentUserResponse,
    LoginRequest,
} from "@/types/auth";

export interface AuthContextValue {
    user: CurrentUserResponse | null;
    isAuthenticated: boolean;
    isLoading: boolean;

    login: (request: LoginRequest) => Promise<void>;
    logout: () => void;
}

export const AuthContext =
    createContext<AuthContextValue | undefined>(undefined);