import {
    Check,
    LoaderCircle,
    Search,
    UserMinus,
    UserPlus,
    X,
} from "lucide-react";
import {
    useEffect,
    useState,
} from "react";

import {
    acceptFriendRequest,
    cancelFriendRequest,
    getFriends,
    getIncomingRequests,
    getOutgoingRequests,
    rejectFriendRequest,
    removeFriend,
    searchUsers,
    sendFriendRequest,
} from "@/api/friendsApi";
import { resolveAvatarUrl } from "@/api/profileApi";
import {
    Avatar,
    AvatarFallback,
    AvatarImage,
} from "@/components/ui/avatar";
import { Button } from "@/components/ui/button";
import {
    Card,
    CardContent,
    CardDescription,
    CardHeader,
    CardTitle,
} from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Skeleton } from "@/components/ui/skeleton";
import type {
    Friend,
    FriendRequest,
    FriendUser,
    UserSearchResult,
} from "@/types/friend";

function getInitials(name: string): string {
    const parts = name
        .trim()
        .split(/\s+/)
        .filter(Boolean);

    if (parts.length === 0) {
        return "?";
    }

    if (parts.length === 1) {
        return parts[0]
            .slice(0, 2)
            .toUpperCase();
    }

    return (
        parts[0][0] +
        parts[parts.length - 1][0]
    ).toUpperCase();
}

function UserAvatar({
                        user,
                    }: {
    user: FriendUser | UserSearchResult;
}) {
    return (
        <Avatar className="size-11">
            <AvatarImage
                src={
                    resolveAvatarUrl(
                        user.avatarUrl,
                    )
                }
                alt={user.displayName}
            />

            <AvatarFallback>
                {getInitials(
                    user.displayName,
                )}
            </AvatarFallback>
        </Avatar>
    );
}

export default function FriendsPage() {
    const [friends, setFriends] =
        useState<Friend[]>([]);

    const [incoming, setIncoming] =
        useState<FriendRequest[]>([]);

    const [outgoing, setOutgoing] =
        useState<FriendRequest[]>([]);

    const [
        searchResults,
        setSearchResults,
    ] = useState<UserSearchResult[]>([]);

    const [query, setQuery] =
        useState("");

    const [isLoading, setIsLoading] =
        useState(true);

    const [isSearching, setIsSearching] =
        useState(false);

    const [activeAction, setActiveAction] =
        useState<string | null>(null);

    const [error, setError] =
        useState<string | null>(null);

    async function loadFriendData() {
        const [
            friendData,
            incomingData,
            outgoingData,
        ] = await Promise.all([
            getFriends(),
            getIncomingRequests(),
            getOutgoingRequests(),
        ]);

        setFriends(friendData);
        setIncoming(incomingData);
        setOutgoing(outgoingData);
    }

    useEffect(() => {
        async function load() {
            try {
                setIsLoading(true);
                setError(null);

                await loadFriendData();
            } catch {
                setError(
                    "Unable to load friend information.",
                );
            } finally {
                setIsLoading(false);
            }
        }

        void load();
    }, []);

    async function handleSearch() {
        const normalizedQuery =
            query.trim();

        if (!normalizedQuery) {
            setSearchResults([]);
            return;
        }

        try {
            setIsSearching(true);
            setError(null);

            const result =
                await searchUsers(
                    normalizedQuery,
                    0,
                    10,
                );

            setSearchResults(
                result.content,
            );
        } catch {
            setError(
                "Unable to search users.",
            );
        } finally {
            setIsSearching(false);
        }
    }

    async function runAction(
        actionKey: string,
        action: () => Promise<unknown>,
    ) {
        try {
            setActiveAction(actionKey);
            setError(null);

            await action();
            await loadFriendData();
        } catch {
            setError(
                "The requested friend action could not be completed.",
            );
        } finally {
            setActiveAction(null);
        }
    }

    function isFriend(userId: string) {
        return friends.some(
            (item) =>
                item.friend.id === userId,
        );
    }

    function findOutgoing(
        userId: string,
    ) {
        return outgoing.find(
            (request) =>
                request.addressee.id ===
                userId,
        );
    }

    function findIncoming(
        userId: string,
    ) {
        return incoming.find(
            (request) =>
                request.requester.id ===
                userId,
        );
    }

    if (isLoading) {
        return (
            <div className="space-y-6">
                <div className="space-y-2">
                    <Skeleton className="h-8 w-40" />
                    <Skeleton className="h-5 w-72" />
                </div>

                <Skeleton className="h-40 w-full" />
                <Skeleton className="h-52 w-full" />
                <Skeleton className="h-52 w-full" />
            </div>
        );
    }

    return (
        <div className="space-y-6">
            <div>
                <h1 className="text-2xl font-semibold">
                    Friends
                </h1>

                <p className="text-muted-foreground">
                    Find people, manage friend
                    requests, and stay connected.
                </p>
            </div>

            {error && (
                <div className="rounded-md border p-3 text-sm text-destructive">
                    {error}
                </div>
            )}

            <Card>
                <CardHeader>
                    <CardTitle>
                        Find people
                    </CardTitle>

                    <CardDescription>
                        Search by username or
                        display name.
                    </CardDescription>
                </CardHeader>

                <CardContent className="space-y-4">
                    <div className="flex gap-2">
                        <Input
                            value={query}
                            placeholder="Search users..."
                            onChange={(event) =>
                                setQuery(
                                    event.target.value,
                                )
                            }
                            onKeyDown={(event) => {
                                if (
                                    event.key ===
                                    "Enter"
                                ) {
                                    void handleSearch();
                                }
                            }}
                        />

                        <Button
                            onClick={() =>
                                void handleSearch()
                            }
                            disabled={isSearching}
                        >
                            {isSearching ? (
                                <LoaderCircle className="animate-spin" />
                            ) : (
                                <Search />
                            )}

                            Search
                        </Button>
                    </div>

                    {searchResults.map(
                        (user) => {
                            const friend =
                                isFriend(
                                    user.id,
                                );

                            const sentRequest =
                                findOutgoing(
                                    user.id,
                                );

                            const receivedRequest =
                                findIncoming(
                                    user.id,
                                );

                            return (
                                <div
                                    key={user.id}
                                    className="flex items-center justify-between gap-4 rounded-lg border p-3"
                                >
                                    <div className="flex min-w-0 items-center gap-3">
                                        <UserAvatar
                                            user={
                                                user
                                            }
                                        />

                                        <div className="min-w-0">
                                            <div className="truncate font-medium">
                                                {
                                                    user.displayName
                                                }
                                            </div>

                                            <div className="truncate text-sm text-muted-foreground">
                                                @
                                                {
                                                    user.username
                                                }
                                            </div>
                                        </div>
                                    </div>

                                    {friend ? (
                                        <Button
                                            variant="outline"
                                            disabled
                                        >
                                            <Check />
                                            Friends
                                        </Button>
                                    ) : sentRequest ? (
                                        <Button
                                            variant="outline"
                                            disabled
                                        >
                                            Request sent
                                        </Button>
                                    ) : receivedRequest ? (
                                        <Button
                                            onClick={() =>
                                                void runAction(
                                                    `accept:${receivedRequest.id}`,
                                                    () =>
                                                        acceptFriendRequest(
                                                            receivedRequest.id,
                                                        ),
                                                )
                                            }
                                            disabled={
                                                activeAction !==
                                                null
                                            }
                                        >
                                            <Check />
                                            Accept
                                        </Button>
                                    ) : (
                                        <Button
                                            onClick={() =>
                                                void runAction(
                                                    `send:${user.id}`,
                                                    () =>
                                                        sendFriendRequest(
                                                            user.id,
                                                        ),
                                                )
                                            }
                                            disabled={
                                                activeAction !==
                                                null
                                            }
                                        >
                                            {activeAction ===
                                            `send:${user.id}` ? (
                                                <LoaderCircle className="animate-spin" />
                                            ) : (
                                                <UserPlus />
                                            )}

                                            Add friend
                                        </Button>
                                    )}
                                </div>
                            );
                        },
                    )}

                    {searchResults.length ===
                        0 &&
                        query.trim() !== "" &&
                        !isSearching && (
                            <p className="text-sm text-muted-foreground">
                                No users found.
                            </p>
                        )}
                </CardContent>
            </Card>

            <div className="grid gap-6 lg:grid-cols-2">
                <Card>
                    <CardHeader>
                        <CardTitle>
                            Friend requests
                        </CardTitle>

                        <CardDescription>
                            Requests waiting for
                            your response.
                        </CardDescription>
                    </CardHeader>

                    <CardContent className="space-y-3">
                        {incoming.length === 0 ? (
                            <p className="text-sm text-muted-foreground">
                                No incoming friend
                                requests.
                            </p>
                        ) : (
                            incoming.map(
                                (request) => (
                                    <div
                                        key={
                                            request.id
                                        }
                                        className="flex items-center justify-between gap-3 rounded-lg border p-3"
                                    >
                                        <div className="flex min-w-0 items-center gap-3">
                                            <UserAvatar
                                                user={
                                                    request.requester
                                                }
                                            />

                                            <div className="min-w-0">
                                                <div className="truncate font-medium">
                                                    {
                                                        request
                                                            .requester
                                                            .displayName
                                                    }
                                                </div>

                                                <div className="truncate text-sm text-muted-foreground">
                                                    @
                                                    {
                                                        request
                                                            .requester
                                                            .username
                                                    }
                                                </div>
                                            </div>
                                        </div>

                                        <div className="flex gap-2">
                                            <Button
                                                size="icon"
                                                onClick={() =>
                                                    void runAction(
                                                        `accept:${request.id}`,
                                                        () =>
                                                            acceptFriendRequest(
                                                                request.id,
                                                            ),
                                                    )
                                                }
                                                disabled={
                                                    activeAction !==
                                                    null
                                                }
                                                aria-label="Accept request"
                                            >
                                                <Check />
                                            </Button>

                                            <Button
                                                size="icon"
                                                variant="outline"
                                                onClick={() =>
                                                    void runAction(
                                                        `reject:${request.id}`,
                                                        () =>
                                                            rejectFriendRequest(
                                                                request.id,
                                                            ),
                                                    )
                                                }
                                                disabled={
                                                    activeAction !==
                                                    null
                                                }
                                                aria-label="Reject request"
                                            >
                                                <X />
                                            </Button>
                                        </div>
                                    </div>
                                ),
                            )
                        )}
                    </CardContent>
                </Card>

                <Card>
                    <CardHeader>
                        <CardTitle>
                            Sent requests
                        </CardTitle>

                        <CardDescription>
                            Friend requests waiting
                            for another user.
                        </CardDescription>
                    </CardHeader>

                    <CardContent className="space-y-3">
                        {outgoing.length === 0 ? (
                            <p className="text-sm text-muted-foreground">
                                No pending sent
                                requests.
                            </p>
                        ) : (
                            outgoing.map(
                                (request) => (
                                    <div
                                        key={
                                            request.id
                                        }
                                        className="flex items-center justify-between gap-3 rounded-lg border p-3"
                                    >
                                        <div className="flex min-w-0 items-center gap-3">
                                            <UserAvatar
                                                user={
                                                    request.addressee
                                                }
                                            />

                                            <div className="min-w-0">
                                                <div className="truncate font-medium">
                                                    {
                                                        request
                                                            .addressee
                                                            .displayName
                                                    }
                                                </div>

                                                <div className="truncate text-sm text-muted-foreground">
                                                    @
                                                    {
                                                        request
                                                            .addressee
                                                            .username
                                                    }
                                                </div>
                                            </div>
                                        </div>

                                        <Button
                                            variant="outline"
                                            size="sm"
                                            onClick={() =>
                                                void runAction(
                                                    `cancel:${request.id}`,
                                                    () =>
                                                        cancelFriendRequest(
                                                            request.id,
                                                        ),
                                                )
                                            }
                                            disabled={
                                                activeAction !==
                                                null
                                            }
                                        >
                                            <X />
                                            Cancel
                                        </Button>
                                    </div>
                                ),
                            )
                        )}
                    </CardContent>
                </Card>
            </div>

            <Card>
                <CardHeader>
                    <CardTitle>
                        Your friends
                    </CardTitle>

                    <CardDescription>
                        {friends.length}{" "}
                        {friends.length === 1
                            ? "friend"
                            : "friends"}
                    </CardDescription>
                </CardHeader>

                <CardContent className="space-y-3">
                    {friends.length === 0 ? (
                        <p className="text-sm text-muted-foreground">
                            You have not added any
                            friends yet.
                        </p>
                    ) : (
                        friends.map(
                            (item) => (
                                <div
                                    key={
                                        item.relationshipId
                                    }
                                    className="flex items-center justify-between gap-4 rounded-lg border p-3"
                                >
                                    <div className="flex min-w-0 items-center gap-3">
                                        <UserAvatar
                                            user={
                                                item.friend
                                            }
                                        />

                                        <div className="min-w-0">
                                            <div className="truncate font-medium">
                                                {
                                                    item
                                                        .friend
                                                        .displayName
                                                }
                                            </div>

                                            <div className="truncate text-sm text-muted-foreground">
                                                @
                                                {
                                                    item
                                                        .friend
                                                        .username
                                                }
                                            </div>
                                        </div>
                                    </div>

                                    <Button
                                        variant="outline"
                                        size="sm"
                                        onClick={() =>
                                            void runAction(
                                                `remove:${item.friend.id}`,
                                                () =>
                                                    removeFriend(
                                                        item
                                                            .friend
                                                            .id,
                                                    ),
                                            )
                                        }
                                        disabled={
                                            activeAction !==
                                            null
                                        }
                                    >
                                        <UserMinus />
                                        Remove
                                    </Button>
                                </div>
                            ),
                        )
                    )}
                </CardContent>
            </Card>
        </div>
    );
}