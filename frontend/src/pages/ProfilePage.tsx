import {
    Camera,
    LoaderCircle,
    Pencil,
    Save,
    X,
} from "lucide-react";
import {
    useEffect,
    useRef,
    useState,
} from "react";

import {
    getMyProfile,
    resolveAvatarUrl,
    updateMyAvatar,
    updateMyProfile,
} from "@/api/profileApi";
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
import { Label } from "@/components/ui/label";
import { Skeleton } from "@/components/ui/skeleton";
import { Textarea } from "@/components/ui/textarea";
import type { UserProfile } from "@/types/profile";

function getInitials(
    displayName: string,
): string {
    const words = displayName
        .trim()
        .split(/\s+/)
        .filter(Boolean);

    if (words.length === 0) {
        return "?";
    }

    if (words.length === 1) {
        return words[0]
            .slice(0, 2)
            .toUpperCase();
    }

    return (
        words[0][0] +
        words[words.length - 1][0]
    ).toUpperCase();
}

export default function ProfilePage() {
    const [profile, setProfile] =
        useState<UserProfile | null>(null);

    const [displayName, setDisplayName] =
        useState("");

    const [bio, setBio] =
        useState("");

    const [isEditing, setIsEditing] =
        useState(false);

    const [isLoading, setIsLoading] =
        useState(true);

    const [isSaving, setIsSaving] =
        useState(false);

    const [isUploadingAvatar, setIsUploadingAvatar] =
        useState(false);

    const [error, setError] =
        useState<string | null>(null);

    const [avatarRevision, setAvatarRevision] =
        useState(0);

    const fileInputRef =
        useRef<HTMLInputElement>(null);

    useEffect(() => {
        async function loadProfile() {
            try {
                setIsLoading(true);
                setError(null);

                const data =
                    await getMyProfile();

                setProfile(data);
                setDisplayName(data.displayName);
                setBio(data.bio ?? "");
            } catch {
                setError(
                    "Unable to load your profile.",
                );
            } finally {
                setIsLoading(false);
            }
        }

        void loadProfile();
    }, []);

    function cancelEditing() {
        if (!profile) {
            return;
        }

        setDisplayName(profile.displayName);
        setBio(profile.bio ?? "");
        setError(null);
        setIsEditing(false);
    }

    async function saveProfile() {
        const normalizedDisplayName =
            displayName.trim();

        if (!normalizedDisplayName) {
            setError(
                "Display name must not be empty.",
            );
            return;
        }

        try {
            setIsSaving(true);
            setError(null);

            const updated =
                await updateMyProfile({
                    displayName:
                    normalizedDisplayName,
                    bio:
                        bio.trim().length > 0
                            ? bio.trim()
                            : null,
                });

            setProfile(updated);
            setDisplayName(updated.displayName);
            setBio(updated.bio ?? "");
            setIsEditing(false);
        } catch {
            setError(
                "Unable to update your profile.",
            );
        } finally {
            setIsSaving(false);
        }
    }

    async function handleAvatarChange(
        event:
        React.ChangeEvent<HTMLInputElement>,
    ) {
        const file =
            event.target.files?.[0];

        if (!file) {
            return;
        }

        const allowedTypes = [
            "image/jpeg",
            "image/png",
            "image/webp",
        ];

        if (!allowedTypes.includes(file.type)) {
            setError(
                "Avatar must be a JPEG, PNG, or WebP image.",
            );
            event.target.value = "";
            return;
        }

        if (file.size > 5 * 1024 * 1024) {
            setError(
                "Avatar size must not exceed 5 MB.",
            );
            event.target.value = "";
            return;
        }

        try {
            setIsUploadingAvatar(true);
            setError(null);

            const updated =
                await updateMyAvatar(file);

            setProfile(updated);

            // Endpoint avatar không đổi URL,
            // nên thay revision để tránh browser cache ảnh cũ.
            setAvatarRevision(
                (current) => current + 1,
            );
        } catch {
            setError(
                "Unable to upload avatar.",
            );
        } finally {
            setIsUploadingAvatar(false);
            event.target.value = "";
        }
    }

    if (isLoading) {
        return (
            <div className="mx-auto w-full max-w-3xl space-y-6">
                <div className="space-y-2">
                    <Skeleton className="h-8 w-40" />
                    <Skeleton className="h-5 w-80" />
                </div>

                <Card>
                    <CardContent className="flex gap-6 pt-6">
                        <Skeleton className="size-24 rounded-full" />

                        <div className="flex-1 space-y-3">
                            <Skeleton className="h-7 w-48" />
                            <Skeleton className="h-5 w-32" />
                            <Skeleton className="h-16 w-full" />
                        </div>
                    </CardContent>
                </Card>
            </div>
        );
    }

    if (!profile) {
        return (
            <div className="mx-auto w-full max-w-3xl">
                <Card>
                    <CardHeader>
                        <CardTitle>
                            Profile unavailable
                        </CardTitle>

                        <CardDescription>
                            {error ??
                                "Your profile could not be loaded."}
                        </CardDescription>
                    </CardHeader>
                </Card>
            </div>
        );
    }

    const rawAvatarUrl =
        resolveAvatarUrl(profile.avatarUrl);

    const avatarUrl =
        rawAvatarUrl
            ? `${rawAvatarUrl}?v=${avatarRevision}`
            : undefined;

    return (
        <div className="mx-auto w-full max-w-3xl space-y-6">
            <div>
                <h1 className="text-2xl font-semibold">
                    Profile
                </h1>

                <p className="text-muted-foreground">
                    Manage your public profile
                    information.
                </p>
            </div>

            <Card>
                <CardHeader>
                    <div className="flex items-start justify-between gap-4">
                        <div>
                            <CardTitle>
                                Personal information
                            </CardTitle>

                            <CardDescription>
                                This information can be
                                seen by other users.
                            </CardDescription>
                        </div>

                        {!isEditing && (
                            <Button
                                variant="outline"
                                onClick={() =>
                                    setIsEditing(true)
                                }
                            >
                                <Pencil />
                                Edit profile
                            </Button>
                        )}
                    </div>
                </CardHeader>

                <CardContent className="space-y-8">
                    <div className="flex flex-col gap-5 sm:flex-row sm:items-center">
                        <div className="relative w-fit">
                            <Avatar className="size-24">
                                <AvatarImage
                                    src={avatarUrl}
                                    alt={
                                        profile.displayName
                                    }
                                />

                                <AvatarFallback className="text-xl">
                                    {getInitials(
                                        profile.displayName,
                                    )}
                                </AvatarFallback>
                            </Avatar>

                            <Button
                                type="button"
                                size="icon"
                                variant="secondary"
                                className="absolute -bottom-1 -right-1 rounded-full"
                                disabled={
                                    isUploadingAvatar
                                }
                                onClick={() =>
                                    fileInputRef.current?.click()
                                }
                                aria-label="Change avatar"
                            >
                                {isUploadingAvatar ? (
                                    <LoaderCircle className="animate-spin" />
                                ) : (
                                    <Camera />
                                )}
                            </Button>

                            <input
                                ref={fileInputRef}
                                type="file"
                                accept="image/jpeg,image/png,image/webp"
                                className="hidden"
                                onChange={
                                    handleAvatarChange
                                }
                            />
                        </div>

                        <div>
                            <div className="text-xl font-semibold">
                                {
                                    profile.displayName
                                }
                            </div>

                            <div className="text-sm text-muted-foreground">
                                @{profile.username}
                            </div>

                            <div className="mt-2 text-xs text-muted-foreground">
                                JPEG, PNG or WebP,
                                maximum 5 MB.
                            </div>
                        </div>
                    </div>

                    {error && (
                        <div className="rounded-md border p-3 text-sm text-destructive">
                            {error}
                        </div>
                    )}

                    <div className="space-y-2">
                        <Label htmlFor="username">
                            Username
                        </Label>

                        <Input
                            id="username"
                            value={profile.username}
                            disabled
                        />

                        <p className="text-xs text-muted-foreground">
                            Username cannot be changed
                            from your profile.
                        </p>
                    </div>

                    <div className="space-y-2">
                        <Label htmlFor="display-name">
                            Display name
                        </Label>

                        <Input
                            id="display-name"
                            value={displayName}
                            maxLength={100}
                            disabled={!isEditing}
                            onChange={(event) =>
                                setDisplayName(
                                    event.target.value,
                                )
                            }
                        />
                    </div>

                    <div className="space-y-2">
                        <div className="flex items-center justify-between">
                            <Label htmlFor="bio">
                                Bio
                            </Label>

                            {isEditing && (
                                <span className="text-xs text-muted-foreground">
                                    {bio.length}/500
                                </span>
                            )}
                        </div>

                        <Textarea
                            id="bio"
                            value={bio}
                            maxLength={500}
                            rows={5}
                            disabled={!isEditing}
                            placeholder="Tell others a little about yourself..."
                            onChange={(event) =>
                                setBio(
                                    event.target.value,
                                )
                            }
                        />
                    </div>

                    {isEditing && (
                        <div className="flex justify-end gap-2">
                            <Button
                                type="button"
                                variant="outline"
                                disabled={isSaving}
                                onClick={
                                    cancelEditing
                                }
                            >
                                <X />
                                Cancel
                            </Button>

                            <Button
                                type="button"
                                disabled={isSaving}
                                onClick={() =>
                                    void saveProfile()
                                }
                            >
                                {isSaving ? (
                                    <LoaderCircle className="animate-spin" />
                                ) : (
                                    <Save />
                                )}

                                Save changes
                            </Button>
                        </div>
                    )}
                </CardContent>
            </Card>
        </div>
    );
}