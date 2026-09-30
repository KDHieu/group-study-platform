const ACCESS_TOKEN_KEY = "access_token";

export const tokenStorage = {
    get(): string | null {
        return sessionStorage.getItem(ACCESS_TOKEN_KEY);
    },

    set(token: string): void {
        sessionStorage.setItem(ACCESS_TOKEN_KEY, token);
    },

    remove(): void {
        sessionStorage.removeItem(ACCESS_TOKEN_KEY);
    },
};