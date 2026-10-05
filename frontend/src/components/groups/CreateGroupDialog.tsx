import {
    Globe2,
    Lock,
} from "lucide-react"
import { useState } from "react"
import type { FormEvent } from "react"

import { getApiErrorMessage } from "@/api/error"
import { createGroup } from "@/api/groups"
import { Button } from "@/components/ui/button"
import {
    Dialog,
    DialogContent,
    DialogDescription,
    DialogFooter,
    DialogHeader,
    DialogTitle,
    DialogTrigger,
} from "@/components/ui/dialog"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Textarea } from "@/components/ui/textarea"
import type {
    GroupVisibility,
    StudyGroup,
} from "@/types/group"

interface CreateGroupDialogProps {
    onCreated: (
        group: StudyGroup,
    ) => void
}

export default function CreateGroupDialog({
                                              onCreated,
                                          }: CreateGroupDialogProps) {
    const [open, setOpen] =
        useState(false)

    const [name, setName] =
        useState("")

    const [description, setDescription] =
        useState("")

    const [visibility, setVisibility] =
        useState<GroupVisibility>("PUBLIC")

    const [submitting, setSubmitting] =
        useState(false)

    const [error, setError] =
        useState<string | null>(null)

    function resetForm() {
        setName("")
        setDescription("")
        setVisibility("PUBLIC")
        setError(null)
    }

    function handleOpenChange(
        nextOpen: boolean,
    ) {
        setOpen(nextOpen)

        if (
            !nextOpen &&
            !submitting
        ) {
            resetForm()
        }
    }

    async function handleSubmit(
        event: FormEvent<HTMLFormElement>,
    ) {
        event.preventDefault()

        try {
            setSubmitting(true)
            setError(null)

            const createdGroup =
                await createGroup({
                    name: name.trim(),
                    description:
                        description.trim() ||
                        undefined,
                    visibility,
                })

            resetForm()
            setOpen(false)

            onCreated(
                createdGroup,
            )
        } catch (error) {
            setError(
                getApiErrorMessage(
                    error,
                    "Could not create the study group.",
                ),
            )
        } finally {
            setSubmitting(false)
        }
    }

    return (
        <Dialog
            open={open}
            onOpenChange={
                handleOpenChange
            }
        >
            <DialogTrigger
                render={<Button />}
            >
                Create group
            </DialogTrigger>

            <DialogContent>
                <form
                    onSubmit={
                        handleSubmit
                    }
                >
                    <DialogHeader>
                        <DialogTitle>
                            Create study group
                        </DialogTitle>

                        <DialogDescription>
                            Create a new group
                            and choose who can
                            discover and join it.
                        </DialogDescription>
                    </DialogHeader>

                    <div className="space-y-4 py-4">
                        <div className="space-y-2">
                            <Label htmlFor="group-name">
                                Name
                            </Label>

                            <Input
                                id="group-name"
                                value={name}
                                onChange={(
                                    event,
                                ) =>
                                    setName(
                                        event
                                            .target
                                            .value,
                                    )
                                }
                                placeholder="Software Architecture"
                                required
                                minLength={3}
                                maxLength={100}
                            />
                        </div>

                        <div className="space-y-2">
                            <Label htmlFor="group-description">
                                Description
                            </Label>

                            <Textarea
                                id="group-description"
                                value={
                                    description
                                }
                                onChange={(
                                    event,
                                ) =>
                                    setDescription(
                                        event
                                            .target
                                            .value,
                                    )
                                }
                                placeholder="What will your group study?"
                                maxLength={1000}
                            />
                        </div>

                        <div className="space-y-2">
                            <Label>
                                Visibility
                            </Label>

                            <div className="grid gap-2 sm:grid-cols-2">
                                <Button
                                    type="button"
                                    variant={
                                        visibility ===
                                        "PUBLIC"
                                            ? "default"
                                            : "outline"
                                    }
                                    aria-pressed={
                                        visibility ===
                                        "PUBLIC"
                                    }
                                    onClick={() =>
                                        setVisibility(
                                            "PUBLIC",
                                        )
                                    }
                                >
                                    <Globe2 />
                                    Public
                                </Button>

                                <Button
                                    type="button"
                                    variant={
                                        visibility ===
                                        "PRIVATE"
                                            ? "default"
                                            : "outline"
                                    }
                                    aria-pressed={
                                        visibility ===
                                        "PRIVATE"
                                    }
                                    onClick={() =>
                                        setVisibility(
                                            "PRIVATE",
                                        )
                                    }
                                >
                                    <Lock />
                                    Private
                                </Button>
                            </div>

                            <p className="text-sm text-muted-foreground">
                                {visibility ===
                                "PUBLIC"
                                    ? "Public groups can be discovered and joined immediately."
                                    : "Private groups are visible only to their members. Joining will require owner approval."}
                            </p>
                        </div>

                        {error && (
                            <p className="text-sm text-destructive">
                                {error}
                            </p>
                        )}
                    </div>

                    <DialogFooter>
                        <Button
                            type="submit"
                            disabled={
                                submitting
                            }
                        >
                            {submitting
                                ? "Creating..."
                                : "Create group"}
                        </Button>
                    </DialogFooter>
                </form>
            </DialogContent>
        </Dialog>
    )
}