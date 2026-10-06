import {
    ArrowLeft,
    Loader2,
    MessageCircle,
    Search,
    Send,
    UserRound,
} from "lucide-react"
import {
    useCallback,
    useEffect,
    useMemo,
    useRef,
    useState,
} from "react"
import type {
    FormEvent,
} from "react"
import {
    useNavigate,
    useParams,
} from "react-router-dom"

import {
    directMessageApi,
} from "@/api/directMessageApi"
import {
    getApiErrorMessage,
} from "@/api/error"
import {
    createChatClient,
} from "@/api/chatSocket"
import {
    resolveAvatarUrl,
} from "@/api/profileApi"
import {
    useAuth,
} from "@/auth/useAuth"
import {
    Avatar,
    AvatarFallback,
    AvatarImage,
} from "@/components/ui/avatar"
import {
    Button,
} from "@/components/ui/button"
import {
    Input,
} from "@/components/ui/input"
import type {
    DirectConversation,
    DirectMessage,
} from "@/types/directMessage"

interface MessageState {
    userId: string | null
    items: DirectMessage[]
    page: number
    hasMore: boolean
}

function getDisplayName(
    conversation: DirectConversation,
): string {
    const displayName =
        conversation.displayName
            ?.trim()

    return displayName ||
        conversation.username
}

function getInitials(
    value: string,
): string {
    const parts =
        value
            .trim()
            .split(/\s+/)
            .filter(Boolean)

    if (
        parts.length === 0
    ) {
        return "?"
    }

    if (
        parts.length === 1
    ) {
        return parts[0]
            .slice(
                0,
                2,
            )
            .toUpperCase()
    }

    return (
        parts[0][0] +
        parts[
        parts.length - 1
            ][0]
    ).toUpperCase()
}

function formatMessageTime(
    createdAt: string,
): string {
    return new Date(
        createdAt,
    ).toLocaleTimeString(
        [],
        {
            hour: "2-digit",
            minute: "2-digit",
        },
    )
}

function formatConversationTime(
    createdAt: string,
): string {
    const date =
        new Date(
            createdAt,
        )

    const now =
        new Date()

    const sameDay =
        date.getFullYear() ===
        now.getFullYear() &&
        date.getMonth() ===
        now.getMonth() &&
        date.getDate() ===
        now.getDate()

    if (sameDay) {
        return date
            .toLocaleTimeString(
                [],
                {
                    hour: "2-digit",
                    minute: "2-digit",
                },
            )
    }

    return date
        .toLocaleDateString(
            [],
            {
                month: "short",
                day: "numeric",
            },
        )
}

export default function MessagesPage() {
    const {
        userId:
            selectedUserId,
    } =
        useParams()

    const navigate =
        useNavigate()

    const {
        user,
    } =
        useAuth()

    const currentUserId =
        user?.id ?? null

    const bottomRef =
        useRef<HTMLDivElement | null>(
            null,
        )

    const [
        conversations,
        setConversations,
    ] =
        useState<
            DirectConversation[]
        >([])

    const [
        messageState,
        setMessageState,
    ] =
        useState<MessageState>({
            userId: null,
            items: [],
            page: 0,
            hasMore: false,
        })

    const [
        search,
        setSearch,
    ] =
        useState("")

    const [
        message,
        setMessage,
    ] =
        useState("")

    const [
        loadingConversations,
        setLoadingConversations,
    ] =
        useState(true)

    const [
        loadingOlder,
        setLoadingOlder,
    ] =
        useState(false)

    const [
        sending,
        setSending,
    ] =
        useState(false)

    const [
        connected,
        setConnected,
    ] =
        useState(false)

    const [
        error,
        setError,
    ] =
        useState<
            string | null
        >(null)

    const selectedConversation =
        useMemo(
            () =>
                conversations.find(
                    (
                        conversation,
                    ) =>
                        conversation.userId ===
                        selectedUserId,
                ) ?? null,
            [
                conversations,
                selectedUserId,
            ],
        )

    const filteredConversations =
        useMemo(() => {
            const normalized =
                search
                    .trim()
                    .toLowerCase()

            if (!normalized) {
                return conversations
            }

            return conversations.filter(
                (
                    conversation,
                ) => {
                    const name =
                        getDisplayName(
                            conversation,
                        )
                            .toLowerCase()

                    const username =
                        conversation
                            .username
                            .toLowerCase()

                    return (
                        name.includes(
                            normalized,
                        ) ||
                        username.includes(
                            normalized,
                        )
                    )
                },
            )
        }, [
            conversations,
            search,
        ])

    const visibleMessages =
        messageState.userId ===
        selectedUserId
            ? messageState.items
            : []

    const loadingMessages =
        Boolean(
            selectedUserId,
        ) &&
        messageState.userId !==
        selectedUserId

    function scrollToBottom(
        behavior:
        ScrollBehavior =
        "smooth",
    ) {
        bottomRef.current
            ?.scrollIntoView({
                behavior,
            })
    }

    const applyIncomingMessage =
        useCallback(
            (
                incoming:
                DirectMessage,
            ) => {
                if (
                    !currentUserId
                ) {
                    return
                }

                const otherUserId =
                    incoming.senderId ===
                    currentUserId
                        ? incoming.receiverId
                        : incoming.senderId

                setMessageState(
                    (
                        current,
                    ) => {
                        if (
                            current.userId !==
                            otherUserId
                        ) {
                            return current
                        }

                        const exists =
                            current.items.some(
                                (
                                    item,
                                ) =>
                                    item.id ===
                                    incoming.id,
                            )

                        if (exists) {
                            return current
                        }

                        return {
                            ...current,
                            items: [
                                ...current.items,
                                incoming,
                            ],
                        }
                    },
                )

                setConversations(
                    (
                        current,
                    ) => {
                        const conversation =
                            current.find(
                                (
                                    item,
                                ) =>
                                    item.userId ===
                                    otherUserId,
                            )

                        if (
                            !conversation
                        ) {
                            return current
                        }

                        const updated = {
                            ...conversation,
                            lastMessage:
                            incoming,
                        }

                        return [
                            updated,
                            ...current.filter(
                                (
                                    item,
                                ) =>
                                    item.userId !==
                                    otherUserId,
                            ),
                        ]
                    },
                )

                window.setTimeout(
                    () => {
                        scrollToBottom()
                    },
                    0,
                )
            },
            [
                currentUserId,
            ],
        )

    useEffect(() => {
        let cancelled =
            false

        async function loadConversations() {
            try {
                setLoadingConversations(
                    true,
                )

                setError(null)

                const response =
                    await directMessageApi
                        .getConversations()

                if (cancelled) {
                    return
                }

                setConversations(
                    response,
                )
            } catch (
                requestError
                ) {
                if (
                    cancelled
                ) {
                    return
                }

                setConversations(
                    [],
                )

                setError(
                    getApiErrorMessage(
                        requestError,
                        "Could not load conversations.",
                    ),
                )
            } finally {
                if (
                    !cancelled
                ) {
                    setLoadingConversations(
                        false,
                    )
                }
            }
        }

        void loadConversations()

        return () => {
            cancelled =
                true
        }
    }, [])

    useEffect(() => {
        if (
            !selectedUserId
        ) {
            return
        }

        const currentSelectedUserId =
            selectedUserId

        let cancelled =
            false

        async function loadConversation() {
            try {
                setError(null)

                const response =
                    await directMessageApi
                        .getConversation(
                            currentSelectedUserId,
                            0,
                            30,
                        )

                if (
                    cancelled
                ) {
                    return
                }

                setMessageState({
                    userId:
                    currentSelectedUserId,
                    items: [
                        ...response.content,
                    ].reverse(),
                    page:
                    response.page
                        .number,
                    hasMore:
                        response.page
                            .number +
                        1 <
                        response.page
                            .totalPages,
                })

                window.setTimeout(
                    () =>
                        scrollToBottom(
                            "auto",
                        ),
                    0,
                )
            } catch (
                requestError
                ) {
                if (
                    cancelled
                ) {
                    return
                }

                setMessageState({
                    userId:
                    currentSelectedUserId,
                    items: [],
                    page: 0,
                    hasMore: false,
                })

                setError(
                    getApiErrorMessage(
                        requestError,
                        "Could not load messages.",
                    ),
                )
            }
        }

        void loadConversation()

        return () => {
            cancelled =
                true
        }
    }, [
        selectedUserId,
    ])

    useEffect(() => {
        if (
            !currentUserId
        ) {
            return
        }

        const client =
            createChatClient(
                () => {
                    setConnected(
                        true,
                    )

                    client.subscribe(
                        "/user/queue/messages",
                        (
                            frame,
                        ) => {
                            try {
                                const incoming =
                                    JSON.parse(
                                        frame.body,
                                    ) as DirectMessage

                                applyIncomingMessage(
                                    incoming,
                                )
                            } catch {
                                setError(
                                    "Received an invalid direct message.",
                                )
                            }
                        },
                    )
                },

                (
                    socketError,
                ) => {
                    setError(
                        socketError,
                    )
                },

                () => {
                    setConnected(
                        false,
                    )
                },
            )

        client.activate()

        return () => {
            void client
                .deactivate()
        }
    }, [
        currentUserId,
        applyIncomingMessage,
    ])

    async function loadOlderMessages() {
        if (
            !selectedUserId ||
            messageState.userId !==
            selectedUserId ||
            !messageState.hasMore ||
            loadingOlder
        ) {
            return
        }

        const currentSelectedUserId =
            selectedUserId

        const nextPage =
            messageState.page +
            1

        try {
            setLoadingOlder(
                true,
            )

            const response =
                await directMessageApi
                    .getConversation(
                        currentSelectedUserId,
                        nextPage,
                        30,
                    )

            const olderMessages =
                [
                    ...response.content,
                ].reverse()

            setMessageState(
                (
                    current,
                ) => {
                    if (
                        current.userId !==
                        currentSelectedUserId
                    ) {
                        return current
                    }

                    const existingIds =
                        new Set(
                            current.items.map(
                                (
                                    item,
                                ) =>
                                    item.id,
                            ),
                        )

                    const uniqueOlder =
                        olderMessages.filter(
                            (
                                item,
                            ) =>
                                !existingIds.has(
                                    item.id,
                                ),
                        )

                    return {
                        userId:
                        currentSelectedUserId,
                        items: [
                            ...uniqueOlder,
                            ...current.items,
                        ],
                        page:
                        response.page
                            .number,
                        hasMore:
                            response.page
                                .number +
                            1 <
                            response.page
                                .totalPages,
                    }
                },
            )
        } catch (
            requestError
            ) {
            setError(
                getApiErrorMessage(
                    requestError,
                    "Could not load older messages.",
                ),
            )
        } finally {
            setLoadingOlder(
                false,
            )
        }
    }

    async function handleSubmit(
        event:
        FormEvent<HTMLFormElement>,
    ) {
        event.preventDefault()

        if (
            !selectedUserId ||
            sending
        ) {
            return
        }

        const content =
            message.trim()

        if (!content) {
            return
        }

        const currentSelectedUserId =
            selectedUserId

        try {
            setSending(
                true,
            )

            setError(null)

            const response =
                await directMessageApi
                    .sendMessage(
                        currentSelectedUserId,
                        {
                            content,
                        },
                    )

            setMessage("")

            applyIncomingMessage(
                response,
            )
        } catch (
            requestError
            ) {
            setError(
                getApiErrorMessage(
                    requestError,
                    "Could not send message.",
                ),
            )
        } finally {
            setSending(
                false,
            )
        }
    }

    function openConversation(
        userId: string,
    ) {
        navigate(
            `/messages/${userId}`,
        )
    }

    function closeConversationOnMobile() {
        navigate(
            "/messages",
        )
    }

    return (
        <div className="flex h-full min-h-0 overflow-hidden border-t bg-background">
            <aside
                className={`w-full shrink-0 border-r bg-background md:w-80 lg:w-96 ${
                    selectedConversation
                        ? "hidden md:flex"
                        : "flex"
                } min-h-0 flex-col`}
            >
                <div className="border-b p-4">
                    <div className="mb-4 flex items-center justify-between">
                        <div>
                            <h1 className="text-xl font-semibold">
                                Messages
                            </h1>

                            <p className="text-xs text-muted-foreground">
                                Chat with your friends
                            </p>
                        </div>

                        <div
                            className="flex items-center gap-2 text-xs text-muted-foreground"
                            title={
                                connected
                                    ? "Realtime connected"
                                    : "Realtime connecting"
                            }
                        >
                            <span
                                className={`size-2 rounded-full ${
                                    connected
                                        ? "bg-green-500"
                                        : "bg-muted-foreground"
                                }`}
                            />

                            {connected
                                ? "Live"
                                : "Connecting"}
                        </div>
                    </div>

                    <div className="relative">
                        <Search className="absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />

                        <Input
                            value={
                                search
                            }
                            onChange={(
                                event,
                            ) =>
                                setSearch(
                                    event
                                        .target
                                        .value,
                                )
                            }
                            className="pl-9"
                            placeholder="Search friends..."
                        />
                    </div>
                </div>

                <div className="min-h-0 flex-1 overflow-y-auto">
                    {loadingConversations ? (
                        <div className="flex items-center justify-center py-12 text-sm text-muted-foreground">
                            <Loader2 className="mr-2 size-4 animate-spin" />

                            Loading conversations...
                        </div>
                    ) : filteredConversations.length ===
                    0 ? (
                        <div className="flex flex-col items-center px-6 py-12 text-center">
                            <div className="mb-3 flex size-12 items-center justify-center rounded-full bg-muted">
                                <UserRound className="size-5 text-muted-foreground" />
                            </div>

                            <p className="font-medium">
                                No friends found
                            </p>

                            <p className="mt-1 text-sm text-muted-foreground">
                                Add friends to start messaging.
                            </p>
                        </div>
                    ) : (
                        filteredConversations.map(
                            (
                                conversation,
                            ) => {
                                const name =
                                    getDisplayName(
                                        conversation,
                                    )

                                const avatarUrl =
                                    resolveAvatarUrl(
                                        conversation.avatarUrl,
                                    )

                                const selected =
                                    conversation.userId ===
                                    selectedUserId

                                return (
                                    <button
                                        key={
                                            conversation.userId
                                        }
                                        type="button"
                                        onClick={() =>
                                            openConversation(
                                                conversation.userId,
                                            )
                                        }
                                        className={`flex w-full items-center gap-3 border-b px-4 py-3 text-left transition-colors hover:bg-muted/60 ${
                                            selected
                                                ? "bg-muted"
                                                : ""
                                        }`}
                                    >
                                        <Avatar className="size-11 shrink-0">
                                            <AvatarImage
                                                src={
                                                    avatarUrl ??
                                                    undefined
                                                }
                                                alt={
                                                    name
                                                }
                                            />

                                            <AvatarFallback>
                                                {getInitials(
                                                    name,
                                                )}
                                            </AvatarFallback>
                                        </Avatar>

                                        <div className="min-w-0 flex-1">
                                            <div className="flex items-center justify-between gap-2">
                                                <p className="truncate text-sm font-medium">
                                                    {
                                                        name
                                                    }
                                                </p>

                                                {conversation.lastMessage && (
                                                    <span className="shrink-0 text-[11px] text-muted-foreground">
                                                        {formatConversationTime(
                                                            conversation
                                                                .lastMessage
                                                                .createdAt,
                                                        )}
                                                    </span>
                                                )}
                                            </div>

                                            <p className="mt-0.5 truncate text-xs text-muted-foreground">
                                                {conversation.lastMessage
                                                    ? `${
                                                        conversation
                                                            .lastMessage
                                                            .senderId ===
                                                        currentUserId
                                                            ? "You: "
                                                            : ""
                                                    }${
                                                        conversation
                                                            .lastMessage
                                                            .content
                                                    }`
                                                    : "No messages yet"}
                                            </p>
                                        </div>
                                    </button>
                                )
                            },
                        )
                    )}
                </div>
            </aside>

            {selectedConversation ? (
                <section className="flex min-h-0 min-w-0 flex-1 flex-col bg-background">
                    <header className="flex h-16 shrink-0 items-center justify-between border-b px-4">
                        <div className="flex min-w-0 items-center gap-3">
                            <Button
                                size="icon"
                                variant="ghost"
                                className="md:hidden"
                                title="Back to conversations"
                                onClick={
                                    closeConversationOnMobile
                                }
                            >
                                <ArrowLeft />
                            </Button>

                            <Avatar className="size-10 shrink-0">
                                <AvatarImage
                                    src={
                                        resolveAvatarUrl(
                                            selectedConversation.avatarUrl,
                                        ) ??
                                        undefined
                                    }
                                    alt={
                                        getDisplayName(
                                            selectedConversation,
                                        )
                                    }
                                />

                                <AvatarFallback>
                                    {getInitials(
                                        getDisplayName(
                                            selectedConversation,
                                        ),
                                    )}
                                </AvatarFallback>
                            </Avatar>

                            <div className="min-w-0">
                                <h2 className="truncate text-sm font-semibold sm:text-base">
                                    {getDisplayName(
                                        selectedConversation,
                                    )}
                                </h2>

                                <p className="truncate text-xs text-muted-foreground">
                                    @
                                    {
                                        selectedConversation.username
                                    }
                                </p>
                            </div>
                        </div>
                    </header>

                    <div className="min-h-0 flex-1 overflow-y-auto px-4 py-5">
                        {messageState.userId ===
                            selectedUserId &&
                            messageState.hasMore && (
                                <div className="mb-5 flex justify-center">
                                    <Button
                                        size="sm"
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

                        {loadingMessages ? (
                            <div className="flex h-full items-center justify-center text-sm text-muted-foreground">
                                <Loader2 className="mr-2 size-4 animate-spin" />

                                Loading messages...
                            </div>
                        ) : visibleMessages.length ===
                        0 ? (
                            <div className="flex h-full items-center justify-center">
                                <div className="max-w-sm text-center">
                                    <div className="mx-auto mb-3 flex size-14 items-center justify-center rounded-full bg-muted">
                                        <MessageCircle className="size-6 text-muted-foreground" />
                                    </div>

                                    <p className="font-medium">
                                        Start a conversation
                                    </p>

                                    <p className="mt-1 text-sm text-muted-foreground">
                                        Send a message to{" "}
                                        {getDisplayName(
                                            selectedConversation,
                                        )}
                                        .
                                    </p>
                                </div>
                            </div>
                        ) : (
                            <div className="space-y-2">
                                {visibleMessages.map(
                                    (
                                        chatMessage,
                                    ) => {
                                        const ownMessage =
                                            chatMessage.senderId ===
                                            currentUserId

                                        return (
                                            <div
                                                key={
                                                    chatMessage.id
                                                }
                                                className={`flex ${
                                                    ownMessage
                                                        ? "justify-end"
                                                        : "justify-start"
                                                }`}
                                            >
                                                <div
                                                    className={`max-w-[78%] md:max-w-[65%] ${
                                                        ownMessage
                                                            ? "items-end"
                                                            : "items-start"
                                                    } flex flex-col`}
                                                >
                                                    <div
                                                        className={`break-words rounded-2xl px-4 py-2.5 text-sm ${
                                                            ownMessage
                                                                ? "rounded-br-md bg-primary text-primary-foreground"
                                                                : "rounded-bl-md bg-muted"
                                                        }`}
                                                    >
                                                        {
                                                            chatMessage.content
                                                        }
                                                    </div>

                                                    <span className="mt-1 px-1 text-[11px] text-muted-foreground">
                                                        {formatMessageTime(
                                                            chatMessage.createdAt,
                                                        )}
                                                    </span>
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

                    {error && (
                        <div className="shrink-0 border-t bg-destructive/5 px-4 py-2 text-xs text-destructive">
                            {error}
                        </div>
                    )}

                    <div className="shrink-0 border-t bg-background p-4">
                        <form
                            onSubmit={
                                handleSubmit
                            }
                            className="mx-auto flex max-w-4xl items-center gap-2"
                        >
                            <Input
                                value={
                                    message
                                }
                                onChange={(
                                    event,
                                ) =>
                                    setMessage(
                                        event
                                            .target
                                            .value,
                                    )
                                }
                                maxLength={
                                    2000
                                }
                                autoComplete="off"
                                placeholder={`Message ${getDisplayName(
                                    selectedConversation,
                                )}...`}
                            />

                            <Button
                                type="submit"
                                size="icon"
                                disabled={
                                    sending ||
                                    !message.trim()
                                }
                                title="Send message"
                            >
                                {sending ? (
                                    <Loader2 className="animate-spin" />
                                ) : (
                                    <Send />
                                )}
                            </Button>
                        </form>
                    </div>
                </section>
            ) : (
                <section className="hidden min-w-0 flex-1 items-center justify-center bg-muted/20 md:flex">
                    <div className="max-w-sm text-center">
                        <div className="mx-auto mb-4 flex size-16 items-center justify-center rounded-full bg-background shadow-sm">
                            <MessageCircle className="size-7 text-muted-foreground" />
                        </div>

                        <h2 className="text-lg font-semibold">
                            Your messages
                        </h2>

                        <p className="mt-1 text-sm text-muted-foreground">
                            Select a friend to start chatting.
                        </p>
                    </div>
                </section>
            )}
        </div>
    )
}