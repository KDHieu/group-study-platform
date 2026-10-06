import {
    ArrowLeft,
    Info,
    Lock,
    MessageCircle,
    Search,
    Users,
    Video,
} from "lucide-react"
import {
    useEffect,
    useMemo,
    useState,
} from "react"
import {
    useNavigate,
    useParams,
} from "react-router-dom"

import { getApiErrorMessage } from "@/api/error"
import {
    getGroupMembers,
    getMyGroups,
} from "@/api/groups"
import { useAuth } from "@/auth/useAuth"
import { useCall } from "@/call/call-context"
import GroupChat from "@/components/chat/GroupChat"
import CallRoomsPanel from "@/components/video/CallRoomsPanel"
import { Button } from "@/components/ui/button"
import {
    Dialog,
    DialogContent,
    DialogHeader,
    DialogTitle,
} from "@/components/ui/dialog"
import { Input } from "@/components/ui/input"
import type { CallRoom } from "@/types/callRoom"
import type {
    GroupMember,
    StudyGroup,
} from "@/types/group"

interface MemberState {
    groupId: string | null
    items: GroupMember[]
}

function getGroupInitials(
    name: string,
) {
    const words =
        name
            .trim()
            .split(/\s+/)
            .filter(Boolean)

    if (words.length === 0) {
        return "SG"
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

export default function StudyRoomsPage() {
    const { groupId } =
        useParams()

    const navigate =
        useNavigate()

    const { user } =
        useAuth()

    const {
        activeCall,
        error: callError,
        joinCall,
        leaveCall,
        clearCallError,
    } =
        useCall()

    const [groups, setGroups] =
        useState<StudyGroup[]>([])

    const [
        memberState,
        setMemberState,
    ] =
        useState<MemberState>({
            groupId: null,
            items: [],
        })

    const [search, setSearch] =
        useState("")

    const [loading, setLoading] =
        useState(true)

    const [
        infoOpen,
        setInfoOpen,
    ] =
        useState(false)

    const [
        callRoomsOpen,
        setCallRoomsOpen,
    ] =
        useState(false)

    const [error, setError] =
        useState<string | null>(
            null,
        )

    useEffect(() => {
        async function loadRooms() {
            try {
                setLoading(true)
                setError(null)

                const response =
                    await getMyGroups(
                        "",
                        0,
                        100,
                    )

                setGroups(
                    response.content,
                )
            } catch (error) {
                setGroups([])

                setError(
                    getApiErrorMessage(
                        error,
                        "Could not load your study rooms.",
                    ),
                )
            } finally {
                setLoading(false)
            }
        }

        void loadRooms()
    }, [])

    const selectedGroup =
        useMemo(
            () =>
                groups.find(
                    (group) =>
                        group.id ===
                        groupId,
                ) ?? null,
            [
                groups,
                groupId,
            ],
        )

    const selectedGroupId =
        selectedGroup?.id ?? null

    const members =
        memberState.groupId ===
        selectedGroupId
            ? memberState.items
            : []

    const membersLoading =
        selectedGroupId !== null &&
        memberState.groupId !==
        selectedGroupId

    const filteredGroups =
        useMemo(() => {
            const normalized =
                search
                    .trim()
                    .toLowerCase()

            if (!normalized) {
                return groups
            }

            return groups.filter(
                (group) =>
                    group.name
                        .toLowerCase()
                        .includes(
                            normalized,
                        ),
            )
        }, [
            groups,
            search,
        ])

    useEffect(() => {
        if (!selectedGroupId) {
            return
        }

        const currentGroupId =
            selectedGroupId

        let cancelled = false

        async function loadMembers() {
            try {
                const response =
                    await getGroupMembers(
                        currentGroupId,
                    )

                if (cancelled) {
                    return
                }

                setMemberState({
                    groupId:
                    currentGroupId,
                    items: response,
                })
            } catch {
                if (cancelled) {
                    return
                }

                setMemberState({
                    groupId:
                    currentGroupId,
                    items: [],
                })
            }
        }

        void loadMembers()

        return () => {
            cancelled = true
        }
    }, [selectedGroupId])

    function openRoom(
        id: string,
    ) {
        setInfoOpen(false)
        setCallRoomsOpen(false)

        navigate(
            `/rooms/${id}`,
        )
    }

    function closeRoomOnMobile() {
        setInfoOpen(false)
        setCallRoomsOpen(false)

        navigate(
            "/rooms",
        )
    }

    function openCallRooms() {
        clearCallError()
        setCallRoomsOpen(true)
    }

    function handleCallRoomsOpenChange(
        open: boolean,
    ) {
        setCallRoomsOpen(open)

        if (!open) {
            clearCallError()
        }
    }

    async function handleJoinCallRoom(
        callRoom: CallRoom,
    ) {
        if (!selectedGroup) {
            return
        }

        const currentGroupId =
            selectedGroup.id

        const currentCall =
            activeCall

        const alreadyInSameCall =
            currentCall?.groupId ===
            currentGroupId &&
            currentCall.callRoomId ===
            callRoom.id

        if (alreadyInSameCall) {
            const joined =
                await joinCall(
                    currentGroupId,
                    callRoom,
                )

            if (joined) {
                setCallRoomsOpen(
                    false,
                )
            }

            return
        }

        if (currentCall) {
            const shouldSwitch =
                window.confirm(
                    `You are currently in "${currentCall.callRoomName}". Switch to "${callRoom.name}"?`,
                )

            if (!shouldSwitch) {
                return
            }

            /*
             * Disconnect the current LiveKit room first.
             * The global overlay is unmounted before the
             * new call session becomes active.
             */
            leaveCall()
        }

        const joined =
            await joinCall(
                currentGroupId,
                callRoom,
            )

        if (joined) {
            setCallRoomsOpen(
                false,
            )
        }
    }

    const currentGroupHasActiveCall =
        selectedGroup !== null &&
        activeCall?.groupId ===
        selectedGroup.id

    return (
        <div className="flex h-full min-h-0 overflow-hidden bg-background">
            <aside
                className={`shrink-0 flex-col border-r bg-background ${
                    selectedGroup
                        ? "hidden w-80 md:flex"
                        : "flex w-full md:w-80"
                }`}
            >
                <div className="border-b px-4 pb-3 pt-4">
                    <div className="mb-4">
                        <h1 className="text-2xl font-bold tracking-tight">
                            Study Rooms
                        </h1>

                        <p className="mt-1 text-sm text-muted-foreground">
                            Your group conversations
                        </p>
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
                                    event.target
                                        .value,
                                )
                            }
                            placeholder="Search rooms"
                            className="rounded-full bg-muted/50 pl-9"
                        />
                    </div>
                </div>

                <div className="min-h-0 flex-1 overflow-y-auto p-2">
                    {loading ? (
                        <div className="p-4 text-sm text-muted-foreground">
                            Loading rooms...
                        </div>
                    ) : error ? (
                        <div className="p-4 text-sm text-destructive">
                            {error}
                        </div>
                    ) : filteredGroups.length ===
                    0 ? (
                        <div className="flex h-full flex-col items-center justify-center px-6 text-center">
                            <div className="mb-3 flex size-12 items-center justify-center rounded-full bg-muted">
                                <MessageCircle className="size-5 text-muted-foreground" />
                            </div>

                            <p className="font-medium">
                                {search
                                    ? "No rooms found"
                                    : "No Study Rooms yet"}
                            </p>

                            <p className="mt-1 text-sm text-muted-foreground">
                                {search
                                    ? "Try another search term."
                                    : "Join or create a study group to start chatting."}
                            </p>
                        </div>
                    ) : (
                        <div className="space-y-1">
                            {filteredGroups.map(
                                (
                                    group,
                                ) => {
                                    const active =
                                        group.id ===
                                        groupId

                                    return (
                                        <button
                                            key={
                                                group.id
                                            }
                                            type="button"
                                            onClick={() =>
                                                openRoom(
                                                    group.id,
                                                )
                                            }
                                            className={`flex w-full items-center gap-3 rounded-xl px-3 py-2.5 text-left transition-colors ${
                                                active
                                                    ? "bg-accent"
                                                    : "hover:bg-accent/60"
                                            }`}
                                        >
                                            <div className="flex size-12 shrink-0 items-center justify-center rounded-full bg-primary/10 text-sm font-semibold text-primary">
                                                {getGroupInitials(
                                                    group.name,
                                                )}
                                            </div>

                                            <div className="min-w-0 flex-1">
                                                <div className="flex items-center gap-2">
                                                    <p className="truncate text-sm font-semibold">
                                                        {
                                                            group.name
                                                        }
                                                    </p>

                                                    {group.visibility ===
                                                        "PRIVATE" && (
                                                            <Lock className="size-3 shrink-0 text-muted-foreground" />
                                                        )}

                                                    {activeCall?.groupId ===
                                                        group.id && (
                                                            <span
                                                                className="size-2 shrink-0 rounded-full bg-green-500"
                                                                title="Active call"
                                                            />
                                                        )}
                                                </div>

                                                <p className="mt-0.5 truncate text-xs text-muted-foreground">
                                                    {activeCall?.groupId ===
                                                    group.id
                                                        ? `In call: ${activeCall.callRoomName}`
                                                        : group.description ||
                                                        `Created by ${group.ownerUsername}`}
                                                </p>
                                            </div>
                                        </button>
                                    )
                                },
                            )}
                        </div>
                    )}
                </div>
            </aside>

            {!selectedGroup ? (
                <section className="hidden min-w-0 flex-1 items-center justify-center md:flex">
                    <div className="max-w-sm text-center">
                        <div className="mx-auto mb-4 flex size-16 items-center justify-center rounded-full bg-muted">
                            <MessageCircle className="size-7 text-muted-foreground" />
                        </div>

                        <h2 className="text-xl font-semibold">
                            Select a Study Room
                        </h2>

                        <p className="mt-2 text-sm text-muted-foreground">
                            Choose a study group
                            from the conversation
                            list to start chatting.
                        </p>
                    </div>
                </section>
            ) : (
                <>
                    <section className="flex min-w-0 flex-1 flex-col bg-background">
                        <header className="flex h-16 shrink-0 items-center justify-between border-b px-3 sm:px-4">
                            <div className="flex min-w-0 items-center gap-2 sm:gap-3">
                                <Button
                                    size="icon"
                                    variant="ghost"
                                    className="shrink-0 rounded-full md:hidden"
                                    title="Back to Study Rooms"
                                    onClick={
                                        closeRoomOnMobile
                                    }
                                >
                                    <ArrowLeft />
                                </Button>

                                <div className="flex size-10 shrink-0 items-center justify-center rounded-full bg-primary/10 text-sm font-semibold text-primary">
                                    {getGroupInitials(
                                        selectedGroup.name,
                                    )}
                                </div>

                                <div className="min-w-0">
                                    <div className="flex items-center gap-2">
                                        <h2 className="truncate text-sm font-semibold sm:text-base">
                                            {
                                                selectedGroup.name
                                            }
                                        </h2>

                                        {currentGroupHasActiveCall && (
                                            <span className="size-2 shrink-0 rounded-full bg-green-500" />
                                        )}
                                    </div>

                                    <p className="truncate text-xs text-muted-foreground">
                                        {currentGroupHasActiveCall
                                            ? `In call: ${activeCall.callRoomName}`
                                            : membersLoading
                                                ? "Loading members..."
                                                : `${members.length} ${
                                                    members.length ===
                                                    1
                                                        ? "member"
                                                        : "members"
                                                }`}
                                    </p>
                                </div>
                            </div>

                            <div className="flex shrink-0 items-center gap-1">
                                <Button
                                    size="icon"
                                    variant={
                                        currentGroupHasActiveCall
                                            ? "secondary"
                                            : "ghost"
                                    }
                                    className="rounded-full"
                                    title="Call rooms"
                                    onClick={
                                        openCallRooms
                                    }
                                >
                                    <Video />
                                </Button>

                                <Button
                                    size="icon"
                                    variant={
                                        infoOpen
                                            ? "secondary"
                                            : "ghost"
                                    }
                                    className="rounded-full"
                                    title="Room information"
                                    onClick={() =>
                                        setInfoOpen(
                                            (
                                                current,
                                            ) =>
                                                !current,
                                        )
                                    }
                                >
                                    <Info />
                                </Button>
                            </div>
                        </header>

                        <div className="min-h-0 flex-1 overflow-hidden">
                            {user?.id ? (
                                <GroupChat
                                    groupId={
                                        selectedGroup.id
                                    }
                                    currentUserId={
                                        user.id
                                    }
                                />
                            ) : null}
                        </div>
                    </section>

                    {infoOpen && (
                        <aside className="hidden w-72 shrink-0 overflow-y-auto border-l bg-background xl:block">
                            <div className="border-b px-5 py-6 text-center">
                                <div className="mx-auto mb-3 flex size-20 items-center justify-center rounded-full bg-primary/10 text-xl font-semibold text-primary">
                                    {getGroupInitials(
                                        selectedGroup.name,
                                    )}
                                </div>

                                <h3 className="font-semibold">
                                    {
                                        selectedGroup.name
                                    }
                                </h3>

                                <div className="mt-2 flex items-center justify-center gap-1 text-xs text-muted-foreground">
                                    {selectedGroup.visibility ===
                                    "PRIVATE" ? (
                                        <>
                                            <Lock className="size-3" />
                                            Private group
                                        </>
                                    ) : (
                                        "Public group"
                                    )}
                                </div>

                                <p className="mt-3 text-sm text-muted-foreground">
                                    {selectedGroup.description ||
                                        "No description provided."}
                                </p>
                            </div>

                            <div className="p-4">
                                <div className="mb-3 flex items-center gap-2">
                                    <Users className="size-4" />

                                    <h4 className="text-sm font-semibold">
                                        Members
                                    </h4>

                                    {!membersLoading && (
                                        <span className="ml-auto text-xs text-muted-foreground">
                                            {
                                                members.length
                                            }
                                        </span>
                                    )}
                                </div>

                                {membersLoading ? (
                                    <p className="text-sm text-muted-foreground">
                                        Loading members...
                                    </p>
                                ) : (
                                    <div className="space-y-1">
                                        {members.map(
                                            (
                                                member,
                                            ) => (
                                                <div
                                                    key={
                                                        member.userId
                                                    }
                                                    className="flex items-center gap-3 rounded-xl p-2 hover:bg-accent/60"
                                                >
                                                    <div className="flex size-9 shrink-0 items-center justify-center rounded-full bg-muted text-xs font-medium">
                                                        {getGroupInitials(
                                                            member.username,
                                                        )}
                                                    </div>

                                                    <div className="min-w-0 flex-1">
                                                        <p className="truncate text-sm font-medium">
                                                            {
                                                                member.username
                                                            }
                                                        </p>

                                                        <p className="text-xs text-muted-foreground">
                                                            {
                                                                member.role
                                                            }
                                                        </p>
                                                    </div>
                                                </div>
                                            ),
                                        )}
                                    </div>
                                )}
                            </div>
                        </aside>
                    )}
                </>
            )}

            {selectedGroup && (
                <Dialog
                    open={
                        callRoomsOpen
                    }
                    onOpenChange={
                        handleCallRoomsOpenChange
                    }
                >
                    <DialogContent className="h-[85vh] w-[95vw] max-w-3xl gap-0 overflow-hidden p-0">
                        <DialogHeader className="sr-only">
                            <DialogTitle>
                                {
                                    selectedGroup.name
                                }{" "}
                                call rooms
                            </DialogTitle>
                        </DialogHeader>

                        <div className="min-h-0 flex-1">
                            {callError && (
                                <div className="border-b bg-destructive/10 px-4 py-3 text-sm text-destructive">
                                    {
                                        callError
                                    }
                                </div>
                            )}

                            <CallRoomsPanel
                                groupId={
                                    selectedGroup.id
                                }
                                onJoinRoom={
                                    handleJoinCallRoom
                                }
                            />
                        </div>
                    </DialogContent>
                </Dialog>
            )}
        </div>
    )
}