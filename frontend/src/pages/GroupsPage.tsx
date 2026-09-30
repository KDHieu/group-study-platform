import { Search, Users } from "lucide-react"
import { useEffect, useState } from "react"
import type { FormEvent } from "react"
import { Link } from "react-router-dom"

import { getGroups } from "@/api/groups"
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
import type { StudyGroup } from "@/types/group"

export default function GroupsPage() {
    const [groups, setGroups] = useState<StudyGroup[]>([])
    const [search, setSearch] = useState("")
    const [query, setQuery] = useState("")

    const [page, setPage] = useState(0)
    const [totalPages, setTotalPages] = useState(0)

    const [loading, setLoading] = useState(true)
    const [refreshKey, setRefreshKey] = useState(0)

    useEffect(() => {
        async function loadGroups() {
            try {
                setLoading(true)

                const response = await getGroups(
                    query,
                    page,
                    10,
                )

                setGroups(response.content)
                setTotalPages(response.page.totalPages)
            } finally {
                setLoading(false)
            }
        }

        void loadGroups()
    }, [query, page, refreshKey])

    function handleSearch(
        event: FormEvent<HTMLFormElement>,
    ) {
        event.preventDefault()

        setPage(0)
        setQuery(search.trim())
    }

    return (
        <div className="space-y-6">
            <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
                <div>
                    <h1 className="text-3xl font-semibold tracking-tight">
                        Study Groups
                    </h1>

                    <p className="mt-1 text-muted-foreground">
                        Find groups and study together with others.
                    </p>
                </div>

                <CreateGroupDialog
                    onCreated={() =>
                        setRefreshKey((current) => current + 1)
                    }
                />
            </div>

            <form
                onSubmit={handleSearch}
                className="flex max-w-xl gap-2"
            >
                <Input
                    value={search}
                    onChange={(event) =>
                        setSearch(event.target.value)
                    }
                    placeholder="Search study groups..."
                />

                <Button type="submit">
                    <Search />
                    Search
                </Button>
            </form>

            {loading ? (
                <p className="text-muted-foreground">
                    Loading groups...
                </p>
            ) : groups.length === 0 ? (
                <Card>
                    <CardContent className="flex flex-col items-center gap-3 py-12">
                        <Users className="h-8 w-8 text-muted-foreground" />

                        <p className="text-muted-foreground">
                            No study groups found.
                        </p>
                    </CardContent>
                </Card>
            ) : (
                <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
                    {groups.map((group) => (
                        <Card key={group.id}>
                            <CardHeader>
                                <CardTitle>
                                    <Link
                                        to={`/groups/${group.id}`}
                                        className="hover:underline"
                                    >
                                        {group.name}
                                    </Link>
                                </CardTitle>

                                <CardDescription>
                                    Created by {group.ownerUsername}
                                </CardDescription>
                            </CardHeader>

                            <CardContent>
                                <p className="text-sm text-muted-foreground">
                                    {group.description ||
                                        "No description provided."}
                                </p>
                            </CardContent>
                        </Card>
                    ))}
                </div>
            )}

            {totalPages > 1 && (
                <div className="flex items-center justify-center gap-3">
                    <Button
                        variant="outline"
                        disabled={page === 0}
                        onClick={() =>
                            setPage((current) => current - 1)
                        }
                    >
                        Previous
                    </Button>

                    <span className="text-sm text-muted-foreground">
            Page {page + 1} of {totalPages}
          </span>

                    <Button
                        variant="outline"
                        disabled={page + 1 >= totalPages}
                        onClick={() =>
                            setPage((current) => current + 1)
                        }
                    >
                        Next
                    </Button>
                </div>
            )}
        </div>
    )
}