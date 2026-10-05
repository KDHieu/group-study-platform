import {
    Globe2,
    Lock,
    Search,
    Users,
} from "lucide-react"
import {
    useEffect,
    useState,
} from "react"
import type {
    FormEvent,
} from "react"
import {
    Link,
    useNavigate,
} from "react-router-dom"

import { getApiErrorMessage } from "@/api/error"
import {
    getGroups,
    getMyGroups,
} from "@/api/groups"
import CreateGroupDialog from "@/components/groups/CreateGroupDialog"
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
    StudyGroup,
} from "@/types/group"

type GroupListMode =
    | "DISCOVER"
    | "MINE"

export default function GroupsPage() {
    const navigate =
        useNavigate()

    const [mode, setMode] =
        useState<GroupListMode>(
            "DISCOVER",
        )

    const [groups, setGroups] =
        useState<StudyGroup[]>([])

    const [search, setSearch] =
        useState("")

    const [query, setQuery] =
        useState("")

    const [page, setPage] =
        useState(0)

    const [
        totalPages,
        setTotalPages,
    ] = useState(0)

    const [loading, setLoading] =
        useState(true)

    const [error, setError] =
        useState<string | null>(
            null,
        )

    useEffect(() => {
        async function loadGroups() {
            try {
                setLoading(true)
                setError(null)

                const response =
                    mode === "DISCOVER"
                        ? await getGroups(
                            query,
                            page,
                            10,
                        )
                        : await getMyGroups(
                            query,
                            page,
                            10,
                        )

                setGroups(
                    response.content,
                )

                setTotalPages(
                    response.page
                        .totalPages,
                )
            } catch (error) {
                setGroups([])
                setTotalPages(0)

                setError(
                    getApiErrorMessage(
                        error,
                        "Could not load study groups.",
                    ),
                )
            } finally {
                setLoading(false)
            }
        }

        void loadGroups()
    }, [
        mode,
        query,
        page,
    ])

    function changeMode(
        nextMode: GroupListMode,
    ) {
        if (nextMode === mode) {
            return
        }

        setMode(nextMode)
        setPage(0)
    }

    function handleSearch(
        event: FormEvent<HTMLFormElement>,
    ) {
        event.preventDefault()

        setPage(0)

        setQuery(
            search.trim(),
        )
    }

    const isDiscover =
        mode === "DISCOVER"

    const emptyMessage =
        isDiscover
            ? query
                ? "No matching study groups found. Private groups require an exact name match."
                : "No public study groups found."
            : "You are not a member of any matching study groups."

    return (
        <div className="space-y-6">
            <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
                <div>
                    <h1 className="text-3xl font-semibold tracking-tight">
                        Study Groups
                    </h1>

                    <p className="mt-1 text-muted-foreground">
                        {isDiscover
                            ? "Browse public groups, or find a private group by entering its exact name."
                            : "View all public and private study groups you belong to."}
                    </p>
                </div>

                <CreateGroupDialog
                    onCreated={(
                        group,
                    ) =>
                        navigate(
                            `/groups/${group.id}`,
                        )
                    }
                />
            </div>

            <div className="flex gap-2">
                <Button
                    type="button"
                    variant={
                        isDiscover
                            ? "default"
                            : "outline"
                    }
                    onClick={() =>
                        changeMode(
                            "DISCOVER",
                        )
                    }
                >
                    Discover
                </Button>

                <Button
                    type="button"
                    variant={
                        !isDiscover
                            ? "default"
                            : "outline"
                    }
                    onClick={() =>
                        changeMode(
                            "MINE",
                        )
                    }
                >
                    My Groups
                </Button>
            </div>

            <form
                onSubmit={
                    handleSearch
                }
                className="flex max-w-xl gap-2"
            >
                <Input
                    value={search}
                    onChange={(
                        event,
                    ) =>
                        setSearch(
                            event.target
                                .value,
                        )
                    }
                    placeholder={
                        isDiscover
                            ? "Search public groups or enter an exact private group name..."
                            : "Search my study groups..."
                    }
                />

                <Button type="submit">
                    <Search />
                    Search
                </Button>
            </form>

            {isDiscover && (
                <p className="text-sm text-muted-foreground">
                    Private groups are not recommended automatically.
                    To find one, enter its exact name.
                </p>
            )}

            {error && (
                <p className="text-sm text-destructive">
                    {error}
                </p>
            )}

            {loading ? (
                <p className="text-muted-foreground">
                    Loading groups...
                </p>
            ) : groups.length ===
            0 ? (
                <Card>
                    <CardContent className="flex flex-col items-center gap-3 py-12">
                        <Users className="h-8 w-8 text-muted-foreground" />

                        <p className="text-center text-muted-foreground">
                            {
                                emptyMessage
                            }
                        </p>
                    </CardContent>
                </Card>
            ) : (
                <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
                    {groups.map(
                        (group) => (
                            <Card
                                key={
                                    group.id
                                }
                            >
                                <CardHeader>
                                    <div className="flex items-start justify-between gap-3">
                                        <CardTitle>
                                            <Link
                                                to={`/groups/${group.id}`}
                                                className="hover:underline"
                                            >
                                                {
                                                    group.name
                                                }
                                            </Link>
                                        </CardTitle>

                                        <span className="inline-flex shrink-0 items-center gap-1 rounded-md border px-2 py-1 text-xs text-muted-foreground">
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

                                    <CardDescription>
                                        Created by{" "}
                                        {
                                            group.ownerUsername
                                        }
                                    </CardDescription>
                                </CardHeader>

                                <CardContent>
                                    <p className="text-sm text-muted-foreground">
                                        {group.description ||
                                            "No description provided."}
                                    </p>
                                </CardContent>
                            </Card>
                        ),
                    )}
                </div>
            )}

            {totalPages > 1 && (
                <div className="flex items-center justify-center gap-3">
                    <Button
                        variant="outline"
                        disabled={
                            page === 0 ||
                            loading
                        }
                        onClick={() =>
                            setPage(
                                (
                                    current,
                                ) =>
                                    current -
                                    1,
                            )
                        }
                    >
                        Previous
                    </Button>

                    <span className="text-sm text-muted-foreground">
                        Page{" "}
                        {page + 1} of{" "}
                        {
                            totalPages
                        }
                    </span>

                    <Button
                        variant="outline"
                        disabled={
                            page + 1 >=
                            totalPages ||
                            loading
                        }
                        onClick={() =>
                            setPage(
                                (
                                    current,
                                ) =>
                                    current +
                                    1,
                            )
                        }
                    >
                        Next
                    </Button>
                </div>
            )}
        </div>
    )
}