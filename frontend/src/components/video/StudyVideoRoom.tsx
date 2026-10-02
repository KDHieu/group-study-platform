import {
    useState,
} from "react"
import {
    Loader2,
    Video,
} from "lucide-react"

import {
    LiveKitRoom,
    RoomAudioRenderer,
    VideoConference,
} from "@livekit/components-react"

import "@livekit/components-styles"

import { getApiErrorMessage } from "@/api/error"
import {
    videoApi,
    type VideoTokenResponse,
} from "@/api/videoApi"

import { Button } from "@/components/ui/button"
import {
    Card,
    CardContent,
    CardDescription,
    CardHeader,
    CardTitle,
} from "@/components/ui/card"

interface StudyVideoRoomProps {
    groupId: string
}

export default function StudyVideoRoom({
                                           groupId,
                                       }: StudyVideoRoomProps) {
    const [
        session,
        setSession,
    ] =
        useState<VideoTokenResponse | null>(
            null,
        )

    const [
        joining,
        setJoining,
    ] =
        useState(false)

    const [
        error,
        setError,
    ] =
        useState<string | null>(
            null,
        )

    async function handleJoin() {
        try {
            setJoining(true)
            setError(null)

            const response =
                await videoApi.createJoinToken(
                    groupId,
                )

            setSession(response)
        } catch (error) {
            setError(
                getApiErrorMessage(
                    error,
                    "Could not join the video room.",
                ),
            )
        } finally {
            setJoining(false)
        }
    }

    function handleDisconnected() {
        setSession(null)
    }

    if (!session) {
        return (
            <Card>
                <CardHeader>
                    <CardTitle className="flex items-center gap-2">
                        <Video className="h-5 w-5" />

                        Study room
                    </CardTitle>

                    <CardDescription>
                        Start or join a live video
                        study session with members
                        of this group.
                    </CardDescription>
                </CardHeader>

                <CardContent className="space-y-4">
                    {error && (
                        <p className="text-sm text-destructive">
                            {error}
                        </p>
                    )}

                    <Button
                        disabled={joining}
                        onClick={() =>
                            void handleJoin()
                        }
                    >
                        {joining ? (
                            <>
                                <Loader2 className="animate-spin" />

                                Joining...
                            </>
                        ) : (
                            <>
                                <Video />

                                Join video room
                            </>
                        )}
                    </Button>
                </CardContent>
            </Card>
        )
    }

    return (
        <Card>
            <CardHeader>
                <CardTitle className="flex items-center gap-2">
                    <Video className="h-5 w-5" />

                    Study room
                </CardTitle>

                <CardDescription>
                    Live video session for this
                    study group.
                </CardDescription>
            </CardHeader>

            <CardContent>
                <div
                    data-lk-theme="default"
                    className="h-[640px] overflow-hidden rounded-xl border bg-black"
                >
                    <LiveKitRoom
                        token={
                            session.token
                        }
                        serverUrl={
                            session.serverUrl
                        }
                        connect={true}
                        audio={true}
                        video={true}
                        onDisconnected={
                            handleDisconnected
                        }
                        style={{
                            height:
                                "100%",
                        }}
                    >
                        <VideoConference />

                        <RoomAudioRenderer />
                    </LiveKitRoom>
                </div>
            </CardContent>
        </Card>
    )
}