import { Trash2, UserMinus, UserPlus, Users } from "lucide-react"
import { useEffect, useState } from "react"
import { useNavigate, useParams } from "react-router-dom"
import { getApiErrorMessage } from "@/api/error"

import {
    deleteGroup,
    getGroupById,
    getGroupMembers,
    joinGroup,
    leaveGroup,
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

export default function GroupDetailPage() {
    const { groupId } = useParams()
    const navigate = useNavigate()
    const { user } = useAuth()

    const [group, setGroup] =
        useState<StudyGroup | null>(null)

    const [members, setMembers] =
        useState<GroupMember[]>([])

    const [loading, setLoading] = useState(true)
    const [actionLoading, setActionLoading] = useState(false)
    const [error, setError] = useState<string | null>(null)
    const [refreshKey, setRefreshKey] = useState(0)

    useEffect(() => {
        if (!groupId) {
            return
        }

        const currentGroupId = groupId

        async function loadGroup() {
            try {
                setLoading(true)
                setError(null)

                const [
                    groupResponse,
                    membersResponse,
                ] = await Promise.all([
                    getGroupById(currentGroupId),
                    getGroupMembers(currentGroupId),
                ])

                setGroup(groupResponse)
                setMembers(membersResponse)
            } catch (error) {
                setError(
                    getApiErrorMessage(
                        error,
                        "Could not join this study group.",
                    ),
                )
            } finally {
                setLoading(false)
            }
        }

        void loadGroup()
    }, [groupId, refreshKey])

    const isOwner =
        group?.ownerId === user?.id

    const isMember =
        members.some(
            (member) => member.userId === user?.id,
        )

    async function handleJoin() {
        if (!groupId) {
            return
        }

        try {
            setActionLoading(true)
            setError(null)

            await joinGroup(groupId)

            setRefreshKey((current) => current + 1)
        } catch (error) {
            setError(
                getApiErrorMessage(
                    error,
                    "Could not join this study group.",
                ),
            )
        } finally {
            setActionLoading(false)
        }
    }

    async function handleLeave() {
        if (!groupId) {
            return
        }

        try {
            setActionLoading(true)
            setError(null)

            await leaveGroup(groupId)

            setRefreshKey((current) => current + 1)
        } catch (error) {
            setError(
                getApiErrorMessage(
                    error,
                    "Could not join this study group.",
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

        const confirmed = window.confirm(
            "Are you sure you want to delete this study group?",
        )

        if (!confirmed) {
            return
        }

        try {
            setActionLoading(true)
            setError(null)

            await deleteGroup(groupId)

            navigate("/groups")
        } catch (error) {
            setError(
                getApiErrorMessage(
                    error,
                    "Could not join this study group.",
                ),
            )
        } finally {
            setActionLoading(false)
        }
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
            <p className="text-muted-foreground">
                Study group not found.
            </p>
        )
    }

    return (
        <div className="space-y-6">
            <div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
                <div>
                    <h1 className="text-3xl font-semibold tracking-tight">
                        {group.name}
                    </h1>

                    <p className="mt-2 text-muted-foreground">
                        {group.description ||
                            "No description provided."}
                    </p>

                    <p className="mt-2 text-sm text-muted-foreground">
                        Created by {group.ownerUsername}
                    </p>
                </div>

                <div className="flex gap-2">
                    {isOwner ? (
                        <Button
                            variant="destructive"
                            disabled={actionLoading}
                            onClick={handleDelete}
                        >
                            <Trash2 />
                            Delete group
                        </Button>
                    ) : isMember ? (
                        <Button
                            variant="outline"
                            disabled={actionLoading}
                            onClick={handleLeave}
                        >
                            <UserMinus />
                            Leave group
                        </Button>
                    ) : (
                        <Button
                            disabled={actionLoading}
                            onClick={handleJoin}
                        >
                            <UserPlus />
                            Join group
                        </Button>
                    )}
                </div>
            </div>

            {error && (
                <p className="text-sm text-destructive">
                    {error}
                </p>
            )}

            <Card>
                <CardHeader>
                    <CardTitle className="flex items-center gap-2">
                        <Users className="h-5 w-5" />
                        Members
                    </CardTitle>

                    <CardDescription>
                        {members.length} member
                        {members.length === 1 ? "" : "s"}
                    </CardDescription>
                </CardHeader>

                <CardContent className="space-y-3">
                    {members.map((member) => (
                        <div
                            key={member.userId}
                            className="flex items-center justify-between border-b pb-3 last:border-0"
                        >
                            <div>
                                <p className="font-medium">
                                    {member.username}
                                </p>

                                <p className="text-sm text-muted-foreground">
                                    {member.role}
                                </p>
                            </div>
                        </div>
                    ))}
                </CardContent>
            </Card>
        </div>
    )
}