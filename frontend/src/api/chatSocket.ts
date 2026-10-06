import { Client } from "@stomp/stompjs"

import { tokenStorage } from "@/auth/tokenStorage"

function getWebSocketUrl(): string {
    const apiBaseUrl =
        import.meta.env.VITE_API_BASE_URL ??
        "http://localhost:8080/api"

    const apiOrigin =
        apiBaseUrl.replace(
            /\/api\/?$/,
            "",
        )

    return (
        apiOrigin
            .replace(
                /^http:/,
                "ws:",
            )
            .replace(
                /^https:/,
                "wss:",
            ) + "/ws"
    )
}

export function createChatClient(
    onConnect: () => void,
    onError: (message: string) => void,
    onClose: () => void,
): Client {
    const token =
        tokenStorage.get()

    const client =
        new Client({
            brokerURL:
                getWebSocketUrl(),

            connectHeaders: token
                ? {
                    Authorization:
                        `Bearer ${token}`,
                }
                : {},

            reconnectDelay: 5000,

            heartbeatIncoming: 10000,
            heartbeatOutgoing: 10000,

            onConnect,

            onStompError: (
                frame,
            ) => {
                onError(
                    frame.headers
                        .message ??
                    "STOMP error",
                )
            },

            onWebSocketError:
                () => {
                    onError(
                        "WebSocket connection error",
                    )
                },

            onWebSocketClose:
                () => {
                    onClose()
                },
        })

    return client
}