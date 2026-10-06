import axios from "axios"
import {
    Check,
    Clock3,
    DoorOpen,
    Globe2,
    Lock,
    Trash2,
    UserMinus,
    UserPlus,
    Users,
    X,
} from "lucide-react"
import {
    useEffect,
    useState,
} from "react"
import {
    useNavigate,
    useParams,
} from "react-router-dom"

import { getApiErrorMessage } from "@/api/error"
import {
    approveJoinRequest,
    deleteGroup,
    getGroupById,
    getGroupMembers,
    getMyJoinRequest,
    getPendingJoinRequests,
    joinGroup,
    leaveGroup,
    rejectJoinRequest,
} from "@/api/groups"
import { useAuth } from "@/auth/useAuth"
import { Button } from "@/components/ui/button"
import {
    Card,
    CardContent,
    CardDescription,
    CardHeader,
    CardTitle,
} from "@/components/ui/card"
import type {
    GroupMember,
    StudyGroup,
} from "@/types/group"
import type {
    GroupJoinRequest,
} from "@/types/groupJoinRequest"

export default function GroupDetailPage() {
    const { groupId } =
        useParams()

    const navigate =
        useNavigate()

    const { user } =
        useAuth()

    const [group, setGroup] =
        useState<StudyGroup | null>(
            null,
        )

    const [members, setMembers] =
        useState<GroupMember[]>([])

    const [
        myJoinRequest,
        setMyJoinRequest,
    ] =
        useState<GroupJoinRequest | null>(
            null,
        )

    const [
        pendingRequests,
        setPendingRequests,
    ] =
        useState<GroupJoinRequest[]>([])

    const [loading, setLoading] =
        useState(Boolean(groupId))

    const [
        actionLoading,
        setActionLoading,
    ] = useState(false)

    const [
        requestActionId,
        setRequestActionId,
    ] =
        useState<string | null>(
            null,
        )

    const [error, setError] =
        useState<string | null>(
            null,
        )

    const [
        refreshKey,
        setRefreshKey,
    ] = useState(0)

    useEffect(() => {
        if (!groupId) {
            return
        }

        const currentGroupId =
            groupId

        async function loadGroup() {
            try {
                setLoading(true)
                setError(null)

                setGroup(null)
                setMembers([])
                setMyJoinRequest(null)
                setPendingRequests([])

                const groupResponse =
                    await getGroupById(
                        currentGroupId,
                    )

                setGroup(
                    groupResponse,
                )

                let hasPrivateContentAccess =
                    true

                try {
                    const membersResponse =
                        await getGroupMembers(
                            currentGroupId,
                        )

                    setMembers(
                        membersResponse,
                    )
                } catch (error) {
                    const isPrivateAccessDenied =
                        groupResponse.visibility ===
                        "PRIVATE" &&
                        axios.isAxiosError(
                            error,
                        ) &&
                        error.response
                            ?.status === 403

                    if (
                        isPrivateAccessDenied
                    ) {
                        hasPrivateContentAccess =
                            false

                        setMembers([])
                    } else {
                        throw error
                    }
                }

                const currentUserIsOwner =
                    groupResponse.ownerId ===
                    user?.id

                if (
                    groupResponse.visibility ===
                    "PRIVATE" &&
                    !hasPrivateContentAccess
                ) {
                    const joinRequest =
                        await getMyJoinRequest(
                            currentGroupId,
                        )

                    setMyJoinRequest(
                        joinRequest,
                    )
                }

                if (
                    groupResponse.visibility ===
                    "PRIVATE" &&
                    currentUserIsOwner
                ) {
                    const requests =
                        await getPendingJoinRequests(
                            currentGroupId,
                        )

                    setPendingRequests(
                        requests,
                    )
                }
            } catch (error) {
                if (
                    axios.isAxiosError(
                        error,
                    ) &&
                    error.response
                        ?.status === 404
                ) {
                    setError(
                        "Study group not found.",
                    )

                    return
                }

                setError(
                    getApiErrorMessage(
                        error,
                        "Could not load this study group.",
                    ),
                )
            } finally {
                setLoading(false)
            }
        }

        void loadGroup()
    }, [
        groupId,
        refreshKey,
        user?.id,
    ])

    const isOwner =
        group?.ownerId ===
        user?.id

    const isMember =
        members.some(
            (member) =>
                member.userId ===
                user?.id,
        )

    async function handleJoin() {
        if (!groupId) {
            return
        }

        try {
            setActionLoading(true)
            setError(null)

            await joinGroup(
                groupId,
            )

            setRefreshKey(
                (current) =>
                    current + 1,
            )
        } catch (error) {
            setError(
                getApiErrorMessage(
                    error,
                    group?.visibility ===
                    "PRIVATE"
                        ? "Could not send the join request."
                        : "Could not join this study group.",
                ),
            )
        } finally {
            setActionLoading(false)
        }
    }

    async function handleLeave() {
        if (
            !groupId ||
            !group
        ) {
            return
        }

        try {
            setActionLoading(true)
            setError(null)

            await leaveGroup(
                groupId,
            )

            setRefreshKey(
                (current) =>
                    current + 1,
            )
        } catch (error) {
            setError(
                getApiErrorMessage(
                    error,
                    "Could not leave this study group.",
                ),
            )
        } finally {
            setActionLoading(false)
        }
    }

    async function handleDelete() {
        if (!groupId) {
            return
        }

        const confirmed =
            window.confirm(
                "Are you sure you want to delete this study group?",
            )

        if (!confirmed) {
            return
        }

        try {
            setActionLoading(true)
            setError(null)

            await deleteGroup(
                groupId,
            )

            navigate(
                "/groups",
            )
        } catch (error) {
            setError(
                getApiErrorMessage(
                    error,
                    "Could not delete this study group.",
                ),
            )
        } finally {
            setActionLoading(false)
        }
    }

    async function handleApprove(
        requestId: string,
    ) {
        if (!groupId) {
            return
        }

        try {
            setRequestActionId(
                requestId,
            )

            setError(null)

            await approveJoinRequest(
                groupId,
                requestId,
            )

            setRefreshKey(
                (current) =>
                    current + 1,
            )
        } catch (error) {
            setError(
                getApiErrorMessage(
                    error,
                    "Could not approve this join request.",
                ),
            )
        } finally {
            setRequestActionId(
                null,
            )
        }
    }

    async function handleReject(
        requestId: string,
    ) {
        if (!groupId) {
            return
        }

        try {
            setRequestActionId(
                requestId,
            )

            setError(null)

            await rejectJoinRequest(
                groupId,
                requestId,
            )

            setRefreshKey(
                (current) =>
                    current + 1,
            )
        } catch (error) {
            setError(
                getApiErrorMessage(
                    error,
                    "Could not reject this join request.",
                ),
            )
        } finally {
            setRequestActionId(
                null,
            )
        }
    }

    function handleOpenStudyRoom() {
        if (!groupId) {
            return
        }

        navigate(
            `/groups/${groupId}/room`,
        )
    }

    function renderPrimaryAction() {
        if (!group) {
            return null
        }

        if (isMember) {
            return (
                <Button
                    onClick={
                        handleOpenStudyRoom
                    }
                >
                    <DoorOpen />
                    Open Study Room
                </Button>
            )
        }

        if (
            group.visibility ===
            "PUBLIC"
        ) {
            return (
                <Button
                    disabled={
                        actionLoading
                    }
                    onClick={
                        handleJoin
                    }
                >
                    <UserPlus />

                    {actionLoading
                        ? "Joining..."
                        : "Join group"}
                </Button>
            )
        }

        if (
            myJoinRequest?.status ===
            "PENDING"
        ) {
            return (
                <Button
                    disabled
                    variant="outline"
                >
                    <Clock3 />
                    Request pending
                </Button>
            )
        }

        return (
            <Button
                disabled={
                    actionLoading
                }
                onClick={
                    handleJoin
                }
            >
                <UserPlus />

                {actionLoading
                    ? "Sending..."
                    : myJoinRequest
                        ? "Request again"
                        : "Request to join"}
            </Button>
        )
    }

    if (loading) {
        return (
            <p className="text-muted-foreground">
                Loading group...
            </p>
        )
    }

    if (!group) {
        return (
            <Card>
                <CardHeader>
                    <CardTitle>
                        Study group
                    </CardTitle>

                    <CardDescription>
                        {error ||
                            "Study group not found."}
                    </CardDescription>
                </CardHeader>

                <CardContent>
                    <Button
                        variant="outline"
                        onClick={() =>
                            navigate(
                                "/groups",
                            )
                        }
                    >
                        Back to groups
                    </Button>
                </CardContent>
            </Card>
        )
    }

    const canSeeMembers =
        group.visibility ===
        "PUBLIC" ||
        isMember

    return (
        <div className="space-y-6">
            <div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
                <div>
                    <div className="flex flex-wrap items-center gap-3">
                        <h1 className="text-3xl font-semibold tracking-tight">
                            {
                                group.name
                            }
                        </h1>

                        <span className="inline-flex items-center gap-1 rounded-md border px-2 py-1 text-xs text-muted-foreground">
                            {group.visibility ===
                            "PRIVATE" ? (
                                <>
                                    <Lock className="h-3 w-3" />
                                    Private
                                </>
                            ) : (
                                <>
                                    <Globe2 className="h-3 w-3" />
                                    Public
                                </>
                            )}
                        </span>
                    </div>

                    <p className="mt-2 text-muted-foreground">
                        {group.description ||
                            "No description provided."}
                    </p>

                    <p className="mt-2 text-sm text-muted-foreground">
                        Created by{" "}
                        {
                            group.ownerUsername
                        }
                    </p>
                </div>

                <div className="flex flex-wrap gap-2">
                    {
                        renderPrimaryAction()
                    }

                    {isMember &&
                        !isOwner && (
                            <Button
                                variant="outline"
                                disabled={
                                    actionLoading
                                }
                                onClick={
                                    handleLeave
                                }
                            >
                                <UserMinus />
                                Leave group
                            </Button>
                        )}

                    {isOwner && (
                        <Button
                            variant="destructive"
                            disabled={
                                actionLoading
                            }
                            onClick={
                                handleDelete
                            }
                        >
                            <Trash2 />
                            Delete group
                        </Button>
                    )}
                </div>
            </div>

            {error && (
                <p className="text-sm text-destructive">
                    {error}
                </p>
            )}

            {group.visibility ===
                "PRIVATE" &&
                !isMember &&
                !isOwner && (
                    <Card>
                        <CardHeader>
                            <CardTitle className="flex items-center gap-2">
                                <Lock className="h-5 w-5" />
                                Private group
                            </CardTitle>

                            <CardDescription>
                                Request access
                                to enter this
                                group's Study
                                Room.
                            </CardDescription>
                        </CardHeader>

                        {myJoinRequest && (
                            <CardContent>
                                <p className="text-sm text-muted-foreground">
                                    Request
                                    status:{" "}
                                    <span className="font-medium text-foreground">
                                        {
                                            myJoinRequest.status
                                        }
                                    </span>
                                </p>
                            </CardContent>
                        )}
                    </Card>
                )}

            {isOwner &&
                group.visibility ===
                "PRIVATE" && (
                    <Card>
                        <CardHeader>
                            <CardTitle>
                                Join requests
                            </CardTitle>

                            <CardDescription>
                                Review users
                                requesting
                                access to this
                                Study Room.
                            </CardDescription>
                        </CardHeader>

                        <CardContent className="space-y-3">
                            {pendingRequests.length ===
                            0 ? (
                                <p className="text-sm text-muted-foreground">
                                    No pending
                                    join requests.
                                </p>
                            ) : (
                                pendingRequests.map(
                                    (
                                        request,
                                    ) => (
                                        <div
                                            key={
                                                request.id
                                            }
                                            className="flex flex-col gap-3 border-b pb-3 last:border-0 sm:flex-row sm:items-center sm:justify-between"
                                        >
                                            <div>
                                                <p className="font-medium">
                                                    {
                                                        request.username
                                                    }
                                                </p>

                                                <p className="text-sm text-muted-foreground">
                                                    Pending
                                                    approval
                                                </p>
                                            </div>

                                            <div className="flex gap-2">
                                                <Button
                                                    size="sm"
                                                    disabled={
                                                        requestActionId ===
                                                        request.id
                                                    }
                                                    onClick={() =>
                                                        void handleApprove(
                                                            request.id,
                                                        )
                                                    }
                                                >
                                                    <Check />
                                                    Approve
                                                </Button>

                                                <Button
                                                    size="sm"
                                                    variant="outline"
                                                    disabled={
                                                        requestActionId ===
                                                        request.id
                                                    }
                                                    onClick={() =>
                                                        void handleReject(
                                                            request.id,
                                                        )
                                                    }
                                                >
                                                    <X />
                                                    Reject
                                                </Button>
                                            </div>
                                        </div>
                                    ),
                                )
                            )}
                        </CardContent>
                    </Card>
                )}

            {canSeeMembers && (
                <Card>
                    <CardHeader>
                        <CardTitle className="flex items-center gap-2">
                            <Users className="h-5 w-5" />
                            Members
                        </CardTitle>

                        <CardDescription>
                            {members.length}{" "}
                            member
                            {members.length ===
                            1
                                ? ""
                                : "s"}
                        </CardDescription>
                    </CardHeader>

                    <CardContent className="space-y-3">
                        {members.map(
                            (member) => (
                                <div
                                    key={
                                        member.userId
                                    }
                                    className="flex items-center justify-between border-b pb-3 last:border-0"
                                >
                                    <div>
                                        <p className="font-medium">
                                            {
                                                member.username
                                            }
                                        </p>

                                        <p className="text-sm text-muted-foreground">
                                            {
                                                member.role
                                            }
                                        </p>
                                    </div>
                                </div>
                            ),
                        )}
                    </CardContent>
                </Card>
            )}
        </div>
    )
}