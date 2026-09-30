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

interface CreateGroupDialogProps {
    onCreated: () => void
}

export default function CreateGroupDialog({
                                              onCreated,
                                          }: CreateGroupDialogProps) {
    const [open, setOpen] = useState(false)
    const [name, setName] = useState("")
    const [description, setDescription] = useState("")
    const [submitting, setSubmitting] = useState(false)
    const [error, setError] = useState<string | null>(null)

    async function handleSubmit(
        event: FormEvent<HTMLFormElement>,
    ) {
        event.preventDefault()

        try {
            setSubmitting(true)
            setError(null)

            await createGroup({
                name: name.trim(),
                description: description.trim() || undefined,
            })

            setName("")
            setDescription("")
            setOpen(false)

            onCreated()
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
            onOpenChange={setOpen}
        >
            <DialogTrigger
                render={<Button />}
            >
                Create group
            </DialogTrigger>

            <DialogContent>
                <form onSubmit={handleSubmit}>
                    <DialogHeader>
                        <DialogTitle>
                            Create study group
                        </DialogTitle>

                        <DialogDescription>
                            Create a new group and invite others to study together.
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
                                onChange={(event) =>
                                    setName(event.target.value)
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
                                value={description}
                                onChange={(event) =>
                                    setDescription(event.target.value)
                                }
                                placeholder="What will your group study?"
                                maxLength={1000}
                            />
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
                            disabled={submitting}
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