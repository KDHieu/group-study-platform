import {
    Maximize2,
    Mic,
    MicOff,
    Minimize2,
    MonitorUp,
    PhoneOff,
    Video,
    VideoOff,
} from "lucide-react"
import {
    useRef,
    useState,
} from "react"
import type {
    PointerEvent as ReactPointerEvent,
} from "react"

import {
    LiveKitRoom,
    RoomAudioRenderer,
    useLocalParticipant,
    VideoConference,
} from "@livekit/components-react"

import "@livekit/components-styles"

import { useCall } from "@/call/call-context"
import { Button } from "@/components/ui/button"

interface FloatingPosition {
    x: number
    y: number
}

interface DragState {
    pointerId: number
    offsetX: number
    offsetY: number
}

type MediaAction =
    | "microphone"
    | "camera"
    | "screen"

const EDGE_PADDING = 16

function MiniCallControls() {
    const {
        localParticipant,
        isMicrophoneEnabled,
        isCameraEnabled,
        isScreenShareEnabled,
    } =
        useLocalParticipant()

    const [
        mediaAction,
        setMediaAction,
    ] =
        useState<MediaAction | null>(
            null,
        )

    async function toggleMicrophone() {
        if (mediaAction) {
            return
        }

        try {
            setMediaAction(
                "microphone",
            )

            await localParticipant
                .setMicrophoneEnabled(
                    !isMicrophoneEnabled,
                )
        } finally {
            setMediaAction(null)
        }
    }

    async function toggleCamera() {
        if (mediaAction) {
            return
        }

        try {
            setMediaAction(
                "camera",
            )

            await localParticipant
                .setCameraEnabled(
                    !isCameraEnabled,
                )
        } finally {
            setMediaAction(null)
        }
    }

    async function toggleScreenShare() {
        if (mediaAction) {
            return
        }

        try {
            setMediaAction(
                "screen",
            )

            await localParticipant
                .setScreenShareEnabled(
                    !isScreenShareEnabled,
                )
        } finally {
            setMediaAction(null)
        }
    }

    return (
        <div className="flex h-14 shrink-0 items-center justify-center gap-3 border-t border-white/10 bg-black px-3">
            <Button
                size="icon"
                variant="ghost"
                title={
                    isMicrophoneEnabled
                        ? "Mute microphone"
                        : "Unmute microphone"
                }
                disabled={
                    mediaAction !== null
                }
                className={
                    isMicrophoneEnabled
                        ? "size-10 rounded-full bg-white/10 text-white hover:bg-white/20 hover:text-white"
                        : "size-10 rounded-full bg-red-500/15 text-red-400 hover:bg-red-500/25 hover:text-red-300"
                }
                onClick={() =>
                    void toggleMicrophone()
                }
            >
                {isMicrophoneEnabled ? (
                    <Mic />
                ) : (
                    <MicOff />
                )}
            </Button>

            <Button
                size="icon"
                variant="ghost"
                title={
                    isCameraEnabled
                        ? "Turn camera off"
                        : "Turn camera on"
                }
                disabled={
                    mediaAction !== null
                }
                className={
                    isCameraEnabled
                        ? "size-10 rounded-full bg-white/10 text-white hover:bg-white/20 hover:text-white"
                        : "size-10 rounded-full bg-red-500/15 text-red-400 hover:bg-red-500/25 hover:text-red-300"
                }
                onClick={() =>
                    void toggleCamera()
                }
            >
                {isCameraEnabled ? (
                    <Video />
                ) : (
                    <VideoOff />
                )}
            </Button>

            <Button
                size="icon"
                variant="ghost"
                title={
                    isScreenShareEnabled
                        ? "Stop sharing screen"
                        : "Share screen"
                }
                disabled={
                    mediaAction !== null
                }
                className={
                    isScreenShareEnabled
                        ? "size-10 rounded-full bg-blue-500 text-white hover:bg-blue-500/90 hover:text-white"
                        : "size-10 rounded-full bg-white/10 text-white hover:bg-white/20 hover:text-white"
                }
                onClick={() =>
                    void toggleScreenShare()
                }
            >
                <MonitorUp />
            </Button>
        </div>
    )
}

export default function GlobalCallOverlay() {
    const {
        activeCall,
        viewMode,
        minimizeCall,
        expandCall,
        leaveCall,
    } =
        useCall()

    const [
        floatingPosition,
        setFloatingPosition,
    ] =
        useState<FloatingPosition | null>(
            null,
        )

    const dragStateRef =
        useRef<DragState | null>(
            null,
        )

    if (!activeCall) {
        return null
    }

    const minimized =
        viewMode === "minimized"

    function handleDragStart(
        event: ReactPointerEvent<HTMLDivElement>,
    ) {
        if (!minimized) {
            return
        }

        const target =
            event.target

        if (
            target instanceof Element &&
            target.closest("button")
        ) {
            return
        }

        const overlay =
            event.currentTarget
                .parentElement

        if (!overlay) {
            return
        }

        const rect =
            overlay.getBoundingClientRect()

        dragStateRef.current = {
            pointerId:
            event.pointerId,
            offsetX:
                event.clientX -
                rect.left,
            offsetY:
                event.clientY -
                rect.top,
        }

        event.currentTarget
            .setPointerCapture(
                event.pointerId,
            )
    }

    function handleDragMove(
        event: ReactPointerEvent<HTMLDivElement>,
    ) {
        const dragState =
            dragStateRef.current

        if (
            !minimized ||
            !dragState ||
            dragState.pointerId !==
            event.pointerId
        ) {
            return
        }

        const overlay =
            event.currentTarget
                .parentElement

        if (!overlay) {
            return
        }

        const rect =
            overlay.getBoundingClientRect()

        const maxX =
            Math.max(
                EDGE_PADDING,
                window.innerWidth -
                rect.width -
                EDGE_PADDING,
            )

        const maxY =
            Math.max(
                EDGE_PADDING,
                window.innerHeight -
                rect.height -
                EDGE_PADDING,
            )

        const nextX =
            Math.min(
                Math.max(
                    event.clientX -
                    dragState.offsetX,
                    EDGE_PADDING,
                ),
                maxX,
            )

        const nextY =
            Math.min(
                Math.max(
                    event.clientY -
                    dragState.offsetY,
                    EDGE_PADDING,
                ),
                maxY,
            )

        setFloatingPosition({
            x: nextX,
            y: nextY,
        })
    }

    function handleDragEnd(
        event: ReactPointerEvent<HTMLDivElement>,
    ) {
        const dragState =
            dragStateRef.current

        if (
            !dragState ||
            dragState.pointerId !==
            event.pointerId
        ) {
            return
        }

        dragStateRef.current =
            null

        if (
            event.currentTarget
                .hasPointerCapture(
                    event.pointerId,
                )
        ) {
            event.currentTarget
                .releasePointerCapture(
                    event.pointerId,
                )
        }
    }

    const floatingStyle =
        minimized &&
        floatingPosition
            ? {
                left:
                floatingPosition.x,
                top:
                floatingPosition.y,
                right: "auto",
                bottom: "auto",
            }
            : undefined

    return (
        <div
            style={
                floatingStyle
            }
            className={
                minimized
                    ? floatingPosition
                        ? "fixed z-50 h-72 w-96 overflow-hidden rounded-2xl border bg-black shadow-2xl"
                        : "fixed bottom-4 right-4 z-50 h-72 w-96 overflow-hidden rounded-2xl border bg-black shadow-2xl"
                    : "fixed inset-4 z-50 overflow-hidden rounded-2xl border bg-black shadow-2xl md:inset-8"
            }
        >
            <LiveKitRoom
                token={
                    activeCall.token
                }
                serverUrl={
                    activeCall.serverUrl
                }
                connect={true}
                audio={true}
                video={true}
                onDisconnected={
                    leaveCall
                }
                data-lk-theme="default"
                className="flex h-full min-h-0 flex-col"
            >
                <div
                    className={`flex h-14 shrink-0 items-center justify-between border-b border-white/10 bg-black px-4 text-white ${
                        minimized
                            ? "cursor-move touch-none select-none"
                            : ""
                    }`}
                    onPointerDown={
                        handleDragStart
                    }
                    onPointerMove={
                        handleDragMove
                    }
                    onPointerUp={
                        handleDragEnd
                    }
                    onPointerCancel={
                        handleDragEnd
                    }
                >
                    <div className="flex min-w-0 items-center gap-3">
                        <div className="flex size-9 shrink-0 items-center justify-center rounded-full bg-white/10">
                            <Video className="size-4" />
                        </div>

                        <div className="min-w-0">
                            <p className="truncate text-sm font-semibold">
                                {
                                    activeCall.callRoomName
                                }
                            </p>

                            <p className="truncate text-xs text-white/60">
                                {minimized
                                    ? "Drag to move"
                                    : "Live study call"}
                            </p>
                        </div>
                    </div>

                    <div className="flex shrink-0 items-center gap-1">
                        {minimized ? (
                            <Button
                                size="icon"
                                variant="ghost"
                                title="Expand call"
                                className="text-white hover:bg-white/10 hover:text-white"
                                onClick={
                                    expandCall
                                }
                            >
                                <Maximize2 />
                            </Button>
                        ) : (
                            <Button
                                size="icon"
                                variant="ghost"
                                title="Minimize call"
                                className="text-white hover:bg-white/10 hover:text-white"
                                onClick={
                                    minimizeCall
                                }
                            >
                                <Minimize2 />
                            </Button>
                        )}

                        <Button
                            size="icon"
                            variant="ghost"
                            title="Leave call"
                            className="text-red-400 hover:bg-red-500/10 hover:text-red-300"
                            onClick={
                                leaveCall
                            }
                        >
                            <PhoneOff />
                        </Button>
                    </div>
                </div>

                <div
                    className={
                        minimized
                            ? "mini-call-conference min-h-0 flex-1 overflow-hidden bg-black"
                            : "min-h-0 flex-1 overflow-hidden bg-black"
                    }
                >
                    <VideoConference />
                </div>

                {minimized && (
                    <MiniCallControls />
                )}

                <RoomAudioRenderer />
            </LiveKitRoom>
        </div>
    )
}