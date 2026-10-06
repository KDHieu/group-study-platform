import {
    Loader2,
    Plus,
    RefreshCw,
    Users,
    Video,
} from "lucide-react"
import {
    useEffect,
    useState,
} from "react"
import type {
    FormEvent,
} from "react"

import { getApiErrorMessage } from "@/api/error"
import { videoApi } from "@/api/videoApi"
import { Button } from "@/components/ui/button"
import {
    Dialog,
    DialogContent,
    DialogDescription,
    DialogFooter,
    DialogHeader,
    DialogTitle,
} from "@/components/ui/dialog"
import { Input } from "@/components/ui/input"
import type {
    CallRoom,
} from "@/types/callRoom"

interface CallRoomsPanelProps {
    groupId: string
    onJoinRoom: (
        callRoom: CallRoom,
    ) => void
}

interface CallRoomState {
    groupId: string | null
    items: CallRoom[]
}

function getInitials(
    name: string,
) {
    const words =
        name
            .trim()
            .split(/\s+/)
            .filter(Boolean)

    if (words.length === 0) {
        return "?"
    }

    if (words.length === 1) {
        return words[0]
            .slice(0, 2)
            .toUpperCase()
    }

    return (
        words[0][0] +
        words[
        words.length - 1
            ][0]
    ).toUpperCase()
}

export default function CallRoomsPanel({
                                           groupId,
                                           onJoinRoom,
                                       }: CallRoomsPanelProps) {
    const [
        roomsState,
        setRoomsState,
    ] =
        useState<CallRoomState>({
            groupId: null,
            items: [],
        })

    const [
        createOpen,
        setCreateOpen,
    ] =
        useState(false)

    const [
        roomName,
        setRoomName,
    ] =
        useState("")

    const [
        creating,
        setCreating,
    ] =
        useState(false)

    const [
        refreshing,
        setRefreshing,
    ] =
        useState(false)

    const [
        error,
        setError,
    ] =
        useState<string | null>(
            null,
        )

    const rooms =
        roomsState.groupId === groupId
            ? roomsState.items
            : []

    const loading =
        roomsState.groupId !== groupId

    useEffect(() => {
        let cancelled = false

        async function loadRooms() {
            try {
                const response =
                    await videoApi
                        .getCallRooms(
                            groupId,
                        )

                if (cancelled) {
                    return
                }

                setRoomsState({
                    groupId,
                    items: response,
                })

                setError(null)
            } catch (error) {
                if (cancelled) {
                    return
                }

                setRoomsState({
                    groupId,
                    items: [],
                })

                setError(
                    getApiErrorMessage(
                        error,
                        "Could not load call rooms.",
                    ),
                )
            }
        }

        void loadRooms()

        const intervalId =
            window.setInterval(
                () => {
                    void loadRooms()
                },
                5000,
            )

        return () => {
            cancelled = true

            window.clearInterval(
                intervalId,
            )
        }
    }, [
        groupId,
    ])

    async function refreshRooms() {
        try {
            setRefreshing(true)

            const response =
                await videoApi
                    .getCallRooms(
                        groupId,
                    )

            setRoomsState({
                groupId,
                items: response,
            })

            setError(null)
        } catch (error) {
            setError(
                getApiErrorMessage(
                    error,
                    "Could not refresh call rooms.",
                ),
            )
        } finally {
            setRefreshing(false)
        }
    }

    async function handleCreateRoom(
        event: FormEvent<HTMLFormElement>,
    ) {
        event.preventDefault()

        const normalizedName =
            roomName.trim()

        if (!normalizedName) {
            return
        }

        try {
            setCreating(true)
            setError(null)

            await videoApi
                .createCallRoom(
                    groupId,
                    {
                        name:
                        normalizedName,
                    },
                )

            const response =
                await videoApi
                    .getCallRooms(
                        groupId,
                    )

            setRoomsState({
                groupId,
                items: response,
            })

            setRoomName("")
            setCreateOpen(false)
        } catch (error) {
            setError(
                getApiErrorMessage(
                    error,
                    "Could not create the call room.",
                ),
            )
        } finally {
            setCreating(false)
        }
    }

    return (
        <>
            <div className="flex h-full min-h-0 flex-col bg-background">
                <div className="flex shrink-0 items-center justify-between border-b px-4 py-3">
                    <div>
                        <h2 className="font-semibold">
                            Call Rooms
                        </h2>

                        <p className="text-xs text-muted-foreground">
                            Join a room or create a new study call.
                        </p>
                    </div>

                    <div className="flex items-center gap-1">
                        <Button
                            size="icon"
                            variant="ghost"
                            title="Refresh call rooms"
                            disabled={
                                refreshing
                            }
                            onClick={() =>
                                void refreshRooms()
                            }
                        >
                            <RefreshCw
                                className={
                                    refreshing
                                        ? "animate-spin"
                                        : ""
                                }
                            />
                        </Button>

                        <Button
                            size="sm"
                            onClick={() =>
                                setCreateOpen(
                                    true,
                                )
                            }
                        >
                            <Plus />
                            Create
                        </Button>
                    </div>
                </div>

                <div className="min-h-0 flex-1 overflow-y-auto p-3">
                    {loading ? (
                        <div className="flex h-32 items-center justify-center gap-2 text-sm text-muted-foreground">
                            <Loader2 className="size-4 animate-spin" />
                            Loading call rooms...
                        </div>
                    ) : error &&
                    rooms.length === 0 ? (
                        <div className="rounded-xl border border-destructive/30 bg-destructive/5 p-4 text-sm text-destructive">
                            {error}
                        </div>
                    ) : rooms.length === 0 ? (
                        <div className="flex min-h-48 flex-col items-center justify-center rounded-xl border border-dashed p-6 text-center">
                            <div className="mb-3 flex size-12 items-center justify-center rounded-full bg-muted">
                                <Video className="size-5" />
                            </div>

                            <h3 className="font-medium">
                                No call rooms yet
                            </h3>

                            <p className="mt-1 max-w-xs text-sm text-muted-foreground">
                                Create a room for a study session,
                                discussion, or small group call.
                            </p>

                            <Button
                                className="mt-4"
                                size="sm"
                                onClick={() =>
                                    setCreateOpen(
                                        true,
                                    )
                                }
                            >
                                <Plus />
                                Create call room
                            </Button>
                        </div>
                    ) : (
                        <div className="space-y-2">
                            {error && (
                                <p className="px-1 text-xs text-destructive">
                                    {error}
                                </p>
                            )}

                            {rooms.map(
                                (
                                    room,
                                ) => (
                                    <div
                                        key={
                                            room.id
                                        }
                                        className="rounded-xl border bg-card p-3 transition-colors hover:bg-muted/40"
                                    >
                                        <div className="flex items-start gap-3">
                                            <div className="flex size-10 shrink-0 items-center justify-center rounded-full bg-primary/10 text-primary">
                                                <Video className="size-4" />
                                            </div>

                                            <div className="min-w-0 flex-1">
                                                <div className="flex items-start justify-between gap-3">
                                                    <div className="min-w-0">
                                                        <p className="truncate text-sm font-semibold">
                                                            {
                                                                room.name
                                                            }
                                                        </p>

                                                        <div className="mt-0.5 flex items-center gap-1 text-xs text-muted-foreground">
                                                            <Users className="size-3" />

                                                            {
                                                                room
                                                                    .participants
                                                                    .length
                                                            }

                                                            {room
                                                                .participants
                                                                .length ===
                                                            1
                                                                ? " participant"
                                                                : " participants"}
                                                        </div>
                                                    </div>

                                                    <Button
                                                        size="sm"
                                                        variant={
                                                            room
                                                                .participants
                                                                .length >
                                                            0
                                                                ? "default"
                                                                : "outline"
                                                        }
                                                        onClick={() =>
                                                            onJoinRoom(
                                                                room,
                                                            )
                                                        }
                                                    >
                                                        <Video />
                                                        Join
                                                    </Button>
                                                </div>

                                                {room
                                                    .participants
                                                    .length >
                                                0 ? (
                                                    <div className="mt-3 space-y-1">
                                                        {room.participants.map(
                                                            (
                                                                participant,
                                                            ) => (
                                                                <div
                                                                    key={
                                                                        participant.sid
                                                                    }
                                                                    className="flex items-center gap-2 rounded-lg px-2 py-1.5 text-sm"
                                                                >
                                                                    <div className="flex size-7 shrink-0 items-center justify-center rounded-full bg-muted text-[10px] font-semibold">
                                                                        {getInitials(
                                                                            participant.name,
                                                                        )}
                                                                    </div>

                                                                    <span className="truncate">
                                                                        {
                                                                            participant.name
                                                                        }
                                                                    </span>

                                                                    <span className="ml-auto size-2 rounded-full bg-green-500" />
                                                                </div>
                                                            ),
                                                        )}
                                                    </div>
                                                ) : (
                                                    <p className="mt-3 text-xs text-muted-foreground">
                                                        Nobody is in this room yet.
                                                    </p>
                                                )}
                                            </div>
                                        </div>
                                    </div>
                                ),
                            )}
                        </div>
                    )}
                </div>
            </div>

            <Dialog
                open={createOpen}
                onOpenChange={
                    setCreateOpen
                }
            >
                <DialogContent>
                    <form
                        onSubmit={
                            handleCreateRoom
                        }
                    >
                        <DialogHeader>
                            <DialogTitle>
                                Create call room
                            </DialogTitle>

                            <DialogDescription>
                                Create a separate call inside this Study Room.
                                Members can choose which call they want to join.
                            </DialogDescription>
                        </DialogHeader>

                        <div className="py-5">
                            <Input
                                autoFocus
                                maxLength={
                                    100
                                }
                                placeholder="e.g. Algorithms discussion"
                                value={
                                    roomName
                                }
                                onChange={(
                                    event,
                                ) =>
                                    setRoomName(
                                        event
                                            .target
                                            .value,
                                    )
                                }
                            />
                        </div>

                        <DialogFooter>
                            <Button
                                type="button"
                                variant="outline"
                                disabled={
                                    creating
                                }
                                onClick={() =>
                                    setCreateOpen(
                                        false,
                                    )
                                }
                            >
                                Cancel
                            </Button>

                            <Button
                                type="submit"
                                disabled={
                                    creating ||
                                    !roomName.trim()
                                }
                            >
                                {creating ? (
                                    <>
                                        <Loader2 className="animate-spin" />
                                        Creating...
                                    </>
                                ) : (
                                    <>
                                        <Plus />
                                        Create room
                                    </>
                                )}
                            </Button>
                        </DialogFooter>
                    </form>
                </DialogContent>
            </Dialog>
        </>
    )
}