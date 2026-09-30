import {
    useEffect,
    useState,
    type ReactNode,
} from "react";

import { authApi } from "@/api/authApi";
import { tokenStorage } from "@/auth/tokenStorage";
import { AuthContext } from "@/auth/auth-context";
import type {
    CurrentUserResponse,
    LoginRequest,
} from "@/types/auth";

interface AuthProviderProps {
    children: ReactNode;
}

export function AuthProvider({
                                 children,
                             }: AuthProviderProps) {
    const [user, setUser] =
        useState<CurrentUserResponse | null>(null);

    const [isLoading, setIsLoading] =
        useState(true);

    useEffect(() => {
        async function restoreAuthentication() {
            const token = tokenStorage.get();

            if (!token) {
                setIsLoading(false);
                return;
            }

            try {
                const currentUser =
                    await authApi.getCurrentUser();

                setUser(currentUser);
            } catch {
                tokenStorage.remove();
                setUser(null);
            } finally {
                setIsLoading(false);
            }
        }

        void restoreAuthentication();
    }, []);

    useEffect(() => {
        function handleUnauthorized() {
            tokenStorage.remove();
            setUser(null);
        }

        window.addEventListener(
            "auth:unauthorized",
            handleUnauthorized,
        );

        return () => {
            window.removeEventListener(
                "auth:unauthorized",
                handleUnauthorized,
            );
        };
    }, []);

    async function login(
        request: LoginRequest,
    ): Promise<void> {
        const response = await authApi.login(request);

        tokenStorage.set(response.accessToken);

        setUser({
            id: response.user.id,
            username: response.user.username,
        });
    }

    function logout(): void {
        tokenStorage.remove();
        setUser(null);
    }

    return (
        <AuthContext.Provider
            value={{
                user,
                isAuthenticated: user !== null,
                isLoading,
                login,
                logout,
            }}
        >
            {children}
        </AuthContext.Provider>
    );
}