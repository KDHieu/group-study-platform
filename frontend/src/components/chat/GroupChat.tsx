import {
    useEffect,
    useRef,
    useState,
} from "react"
import type { Client } from "@stomp/stompjs"
import { Loader2, Send } from "lucide-react"

import { chatApi } from "@/api/chatApi"
import { createChatClient } from "@/api/chatSocket"
import { Button } from "@/components/ui/button"
import {
    Card,
    CardContent,
    CardDescription,
    CardHeader,
    CardTitle,
} from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import type {
    ChatMessage,
    TypingEvent,
} from "@/types/chat"

interface GroupChatProps {
    groupId: string
    currentUserId: string
}

function resolveAvatarUrl(
    avatarUrl: string | null,
): string | null {
    if (!avatarUrl) {
        return null
    }

    if (
        avatarUrl.startsWith("http://") ||
        avatarUrl.startsWith("https://")
    ) {
        return avatarUrl
    }

    const apiBaseUrl =
        import.meta.env.VITE_API_BASE_URL ??
        "http://localhost:8080/api"

    const apiOrigin =
        apiBaseUrl.replace(/\/api\/?$/, "")

    return `${apiOrigin}${
        avatarUrl.startsWith("/") ? "" : "/"
    }${avatarUrl}`
}

function getInitials(
    displayName: string,
): string {
    const parts =
        displayName
            .trim()
            .split(/\s+/)
            .filter(Boolean)

    if (parts.length === 0) {
        return "?"
    }

    if (parts.length === 1) {
        return parts[0]
            .charAt(0)
            .toUpperCase()
    }

    return (
        parts[0].charAt(0) +
        parts[parts.length - 1].charAt(0)
    ).toUpperCase()
}

function formatMessageTime(
    createdAt: string,
): string {
    return new Date(
        createdAt,
    ).toLocaleTimeString([], {
        hour: "2-digit",
        minute: "2-digit",
    })
}

export default function GroupChat({
                                      groupId,
                                      currentUserId,
                                  }: GroupChatProps) {
    const clientRef =
        useRef<Client | null>(null)

    const typingTimeoutRef =
        useRef<number | null>(null)

    const bottomRef =
        useRef<HTMLDivElement | null>(null)

    const [messages, setMessages] =
        useState<ChatMessage[]>([])

    const [message, setMessage] =
        useState("")

    const [typingUsers, setTypingUsers] =
        useState<
            Record<string, TypingEvent>
        >({})

    const [connected, setConnected] =
        useState(false)

    const [loading, setLoading] =
        useState(true)

    const [loadingOlder, setLoadingOlder] =
        useState(false)

    const [page, setPage] =
        useState(0)

    const [hasMore, setHasMore] =
        useState(false)

    const [error, setError] =
        useState<string | null>(null)

    function scrollToBottom(
        behavior: ScrollBehavior = "smooth",
    ) {
        bottomRef.current?.scrollIntoView({
            behavior,
        })
    }

    function appendMessage(
        incoming: ChatMessage,
    ) {
        setMessages((current) => {
            const alreadyExists =
                current.some(
                    (item) =>
                        item.id === incoming.id,
                )

            if (alreadyExists) {
                return current
            }

            return [
                ...current,
                incoming,
            ]
        })
    }

    function updateTypingUser(
        event: TypingEvent,
    ) {
        setTypingUsers((current) => {
            const next = {
                ...current,
            }

            if (event.typing) {
                next[event.userId] =
                    event
            } else {
                delete next[
                    event.userId
                    ]
            }

            return next
        })
    }

    function publishTyping(
        typing: boolean,
    ) {
        const client =
            clientRef.current

        if (!client?.connected) {
            return
        }

        client.publish({
            destination:
                `/app/groups/${groupId}/typing`,

            body: JSON.stringify({
                typing,
            }),
        })
    }

    function handleMessageChange(
        value: string,
    ) {
        setMessage(value)

        if (!connected) {
            return
        }

        publishTyping(
            value.trim().length > 0,
        )

        if (
            typingTimeoutRef.current !==
            null
        ) {
            window.clearTimeout(
                typingTimeoutRef.current,
            )
        }

        if (!value.trim()) {
            typingTimeoutRef.current =
                null

            return
        }

        typingTimeoutRef.current =
            window.setTimeout(() => {
                publishTyping(false)

                typingTimeoutRef.current =
                    null
            }, 1200)
    }

    function sendMessage() {
        const client =
            clientRef.current

        const content =
            message.trim()

        if (
            !client?.connected ||
            !content
        ) {
            return
        }

        client.publish({
            destination:
                `/app/groups/${groupId}/messages`,

            body: JSON.stringify({
                content,
            }),
        })

        publishTyping(false)

        if (
            typingTimeoutRef.current !==
            null
        ) {
            window.clearTimeout(
                typingTimeoutRef.current,
            )

            typingTimeoutRef.current =
                null
        }

        setMessage("")
    }

    async function loadOlderMessages() {
        if (
            loadingOlder ||
            !hasMore
        ) {
            return
        }

        const nextPage =
            page + 1

        try {
            setLoadingOlder(true)

            const response =
                await chatApi.getMessageHistory(
                    groupId,
                    nextPage,
                    30,
                )

            const olderMessages = [
                ...response.content,
            ].reverse()

            setMessages((current) => {
                const existingIds =
                    new Set(
                        current.map(
                            (item) =>
                                item.id,
                        ),
                    )

                const uniqueOlder =
                    olderMessages.filter(
                        (item) =>
                            !existingIds.has(
                                item.id,
                            ),
                    )

                return [
                    ...uniqueOlder,
                    ...current,
                ]
            })

            setPage(
                response.page.number,
            )

            setHasMore(
                response.page.number + 1 <
                response.page
                    .totalPages,
            )
        } catch {
            setError(
                "Could not load older messages.",
            )
        } finally {
            setLoadingOlder(false)
        }
    }

    useEffect(() => {
        let cancelled = false

        async function loadInitialMessages() {
            try {
                setLoading(true)
                setError(null)

                const response =
                    await chatApi.getMessageHistory(
                        groupId,
                        0,
                        30,
                    )

                if (cancelled) {
                    return
                }

                setMessages(
                    [
                        ...response.content,
                    ].reverse(),
                )

                setPage(
                    response.page.number,
                )

                setHasMore(
                    response.page.number +
                    1 <
                    response.page
                        .totalPages,
                )
            } catch {
                if (!cancelled) {
                    setError(
                        "Could not load chat history.",
                    )
                }
            } finally {
                if (!cancelled) {
                    setLoading(false)
                }
            }
        }

        void loadInitialMessages()

        return () => {
            cancelled = true
        }
    }, [groupId])

    useEffect(() => {
        const client =
            createChatClient(
                () => {
                    setConnected(true)
                    setError(null)

                    client.subscribe(
                        `/topic/groups/${groupId}/messages`,
                        (frame) => {
                            try {
                                const incoming =
                                    JSON.parse(
                                        frame.body,
                                    ) as ChatMessage

                                appendMessage(
                                    incoming,
                                )

                                window.setTimeout(
                                    () =>
                                        scrollToBottom(),
                                    0,
                                )
                            } catch {
                                setError(
                                    "Received an invalid chat message.",
                                )
                            }
                        },
                    )

                    client.subscribe(
                        `/topic/groups/${groupId}/typing`,
                        (frame) => {
                            try {
                                const event =
                                    JSON.parse(
                                        frame.body,
                                    ) as TypingEvent

                                updateTypingUser(
                                    event,
                                )
                            } catch {
                                setError(
                                    "Received an invalid typing event.",
                                )
                            }
                        },
                    )
                },

                (socketError) => {
                    setError(socketError)
                },

                () => {
                    setConnected(false)
                    setTypingUsers({})
                },
            )

        clientRef.current =
            client

        client.activate()

        return () => {
            if (
                typingTimeoutRef.current !==
                null
            ) {
                window.clearTimeout(
                    typingTimeoutRef.current,
                )
            }

            if (client.connected) {
                client.publish({
                    destination:
                        `/app/groups/${groupId}/typing`,

                    body: JSON.stringify({
                        typing: false,
                    }),
                })
            }

            void client.deactivate()

            clientRef.current =
                null
        }
    }, [groupId])

    useEffect(() => {
        if (!loading) {
            scrollToBottom("auto")
        }
    }, [loading])

    const visibleTypingUsers =
        Object.values(
            typingUsers,
        ).filter(
            (typingUser) =>
                typingUser.userId !==
                currentUserId,
        )

    return (
        <Card>
            <CardHeader>
                <div className="flex items-start justify-between gap-4">
                    <div>
                        <CardTitle>
                            Group chat
                        </CardTitle>

                        <CardDescription>
                            Chat with members
                            of this study
                            group in real
                            time.
                        </CardDescription>
                    </div>

                    <div className="flex items-center gap-2 text-sm text-muted-foreground">
                        <span
                            className={`h-2 w-2 rounded-full ${
                                connected
                                    ? "bg-green-500"
                                    : "bg-muted-foreground"
                            }`}
                        />

                        {connected
                            ? "Live"
                            : "Connecting..."}
                    </div>
                </div>
            </CardHeader>

            <CardContent className="space-y-4">
                {error && (
                    <p className="text-sm text-destructive">
                        {error}
                    </p>
                )}

                <div className="flex h-[500px] flex-col rounded-lg border">
                    <div className="flex-1 overflow-y-auto p-4">
                        {hasMore && (
                            <div className="mb-4 flex justify-center">
                                <Button
                                    variant="outline"
                                    disabled={
                                        loadingOlder
                                    }
                                    onClick={() =>
                                        void loadOlderMessages()
                                    }
                                >
                                    {loadingOlder ? (
                                        <>
                                            <Loader2 className="animate-spin" />
                                            Loading...
                                        </>
                                    ) : (
                                        "Load older messages"
                                    )}
                                </Button>
                            </div>
                        )}

                        {loading ? (
                            <div className="flex h-full items-center justify-center text-muted-foreground">
                                <Loader2 className="mr-2 animate-spin" />
                                Loading messages...
                            </div>
                        ) : messages.length ===
                        0 ? (
                            <div className="flex h-full items-center justify-center text-center text-sm text-muted-foreground">
                                No messages yet.
                                Start the
                                conversation.
                            </div>
                        ) : (
                            <div className="space-y-4">
                                {messages.map(
                                    (
                                        chatMessage,
                                    ) => {
                                        const ownMessage =
                                            chatMessage
                                                .sender
                                                .id ===
                                            currentUserId

                                        const avatarUrl =
                                            resolveAvatarUrl(
                                                chatMessage
                                                    .sender
                                                    .avatarUrl,
                                            )

                                        return (
                                            <div
                                                key={
                                                    chatMessage.id
                                                }
                                                className={`flex gap-3 ${
                                                    ownMessage
                                                        ? "justify-end"
                                                        : "justify-start"
                                                }`}
                                            >
                                                {!ownMessage && (
                                                    <div className="shrink-0">
                                                        {avatarUrl ? (
                                                            <img
                                                                src={
                                                                    avatarUrl
                                                                }
                                                                alt={
                                                                    chatMessage
                                                                        .sender
                                                                        .displayName
                                                                }
                                                                className="h-9 w-9 rounded-full object-cover"
                                                            />
                                                        ) : (
                                                            <div className="flex h-9 w-9 items-center justify-center rounded-full bg-muted text-xs font-medium">
                                                                {getInitials(
                                                                    chatMessage
                                                                        .sender
                                                                        .displayName,
                                                                )}
                                                            </div>
                                                        )}
                                                    </div>
                                                )}

                                                <div
                                                    className={`max-w-[75%] ${
                                                        ownMessage
                                                            ? "text-right"
                                                            : ""
                                                    }`}
                                                >
                                                    {!ownMessage && (
                                                        <p className="mb-1 text-xs font-medium text-muted-foreground">
                                                            {
                                                                chatMessage
                                                                    .sender
                                                                    .displayName
                                                            }
                                                        </p>
                                                    )}

                                                    <div
                                                        className={`rounded-2xl px-4 py-2 text-sm ${
                                                            ownMessage
                                                                ? "bg-primary text-primary-foreground"
                                                                : "bg-muted"
                                                        }`}
                                                    >
                                                        {chatMessage.type ===
                                                        "TEXT"
                                                            ? chatMessage.content
                                                            : `[${chatMessage.type}]`}
                                                    </div>

                                                    <p className="mt-1 text-xs text-muted-foreground">
                                                        {formatMessageTime(
                                                            chatMessage.createdAt,
                                                        )}
                                                    </p>
                                                </div>
                                            </div>
                                        )
                                    },
                                )}

                                <div
                                    ref={
                                        bottomRef
                                    }
                                />
                            </div>
                        )}
                    </div>

                    <div className="border-t p-3">
                        <div className="min-h-5 pb-1 text-xs text-muted-foreground">
                            {visibleTypingUsers.length >
                            0
                                ? `${visibleTypingUsers
                                    .map(
                                        (
                                            typingUser,
                                        ) =>
                                            typingUser.displayName,
                                    )
                                    .join(
                                        ", ",
                                    )} ${
                                    visibleTypingUsers.length ===
                                    1
                                        ? "is"
                                        : "are"
                                } typing...`
                                : null}
                        </div>

                        <div className="flex gap-2">
                            <Input
                                placeholder="Write a message..."
                                value={
                                    message
                                }
                                disabled={
                                    !connected
                                }
                                maxLength={
                                    2000
                                }
                                onChange={(
                                    event,
                                ) =>
                                    handleMessageChange(
                                        event
                                            .target
                                            .value,
                                    )
                                }
                                onKeyDown={(
                                    event,
                                ) => {
                                    if (
                                        event.key ===
                                        "Enter" &&
                                        !event.shiftKey
                                    ) {
                                        event.preventDefault()
                                        sendMessage()
                                    }
                                }}
                            />

                            <Button
                                disabled={
                                    !connected ||
                                    !message.trim()
                                }
                                onClick={
                                    sendMessage
                                }
                            >
                                <Send />
                                Send
                            </Button>
                        </div>
                    </div>
                </div>
            </CardContent>
        </Card>
    )
}