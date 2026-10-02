import {
    useEffect,
    useRef,
    useState,
} from "react"
import {
    Loader2,
    Mic,
    Send,
    Square,
    X,
} from "lucide-react"

import { Button } from "@/components/ui/button"

interface VoiceRecorderProps {
    disabled?: boolean

    onSend: (
        audio: Blob,
        durationMs: number,
    ) => Promise<void>
}

const MAX_RECORDING_MS =
    5 * 60 * 1000

function formatDuration(
    durationMs: number,
): string {
    const totalSeconds =
        Math.floor(
            durationMs / 1000,
        )

    const minutes =
        Math.floor(
            totalSeconds / 60,
        )

    const seconds =
        totalSeconds % 60

    return `${minutes}:${seconds
        .toString()
        .padStart(2, "0")}`
}

function resolveMimeType(): string {
    const candidates = [
        "audio/webm;codecs=opus",
        "audio/webm",
        "audio/ogg;codecs=opus",
        "audio/ogg",
        "audio/mp4",
    ]

    for (const mimeType of candidates) {
        if (
            MediaRecorder.isTypeSupported(
                mimeType,
            )
        ) {
            return mimeType
        }
    }

    return ""
}

export default function VoiceRecorder({
                                          disabled = false,
                                          onSend,
                                      }: VoiceRecorderProps) {
    const mediaRecorderRef =
        useRef<MediaRecorder | null>(
            null,
        )

    const mediaStreamRef =
        useRef<MediaStream | null>(
            null,
        )

    const chunksRef =
        useRef<BlobPart[]>([])

    const startedAtRef =
        useRef<number | null>(
            null,
        )

    const timerRef =
        useRef<number | null>(
            null,
        )

    const [recording, setRecording] =
        useState(false)

    const [uploading, setUploading] =
        useState(false)

    const [audioBlob, setAudioBlob] =
        useState<Blob | null>(
            null,
        )

    const [previewUrl, setPreviewUrl] =
        useState<string | null>(
            null,
        )

    const [durationMs, setDurationMs] =
        useState(0)

    const [error, setError] =
        useState<string | null>(
            null,
        )

    function clearTimer() {
        if (timerRef.current !== null) {
            window.clearInterval(
                timerRef.current,
            )

            timerRef.current = null
        }
    }

    function stopMediaTracks() {
        mediaStreamRef.current
            ?.getTracks()
            .forEach((track) =>
                track.stop(),
            )

        mediaStreamRef.current = null
    }

    function resetRecording() {
        if (previewUrl) {
            URL.revokeObjectURL(
                previewUrl,
            )
        }

        setPreviewUrl(null)
        setAudioBlob(null)
        setDurationMs(0)
        setError(null)
    }

    async function startRecording() {
        if (
            disabled ||
            recording ||
            uploading
        ) {
            return
        }

        try {
            setError(null)
            resetRecording()

            const stream =
                await navigator.mediaDevices
                    .getUserMedia({
                        audio: true,
                    })

            mediaStreamRef.current =
                stream

            const mimeType =
                resolveMimeType()

            const recorder =
                mimeType
                    ? new MediaRecorder(
                        stream,
                        {
                            mimeType,
                        },
                    )
                    : new MediaRecorder(
                        stream,
                    )

            mediaRecorderRef.current =
                recorder

            chunksRef.current = []

            recorder.ondataavailable = (
                event,
            ) => {
                if (
                    event.data.size > 0
                ) {
                    chunksRef.current.push(
                        event.data,
                    )
                }
            }

            recorder.onstop = () => {
                const startedAt =
                    startedAtRef.current

                const finalDuration =
                    startedAt === null
                        ? durationMs
                        : Math.min(
                            Date.now() -
                            startedAt,
                            MAX_RECORDING_MS,
                        )

                const blob =
                    new Blob(
                        chunksRef.current,
                        {
                            type:
                                recorder.mimeType ||
                                "audio/webm",
                        },
                    )

                const objectUrl =
                    URL.createObjectURL(
                        blob,
                    )

                setAudioBlob(blob)

                setPreviewUrl(
                    objectUrl,
                )

                setDurationMs(
                    finalDuration,
                )

                setRecording(false)

                clearTimer()
                stopMediaTracks()

                startedAtRef.current =
                    null
            }

            recorder.onerror = () => {
                setError(
                    "An error occurred while recording audio.",
                )

                setRecording(false)

                clearTimer()
                stopMediaTracks()
            }

            startedAtRef.current =
                Date.now()

            setDurationMs(0)
            setRecording(true)

            recorder.start()

            timerRef.current =
                window.setInterval(() => {
                    const startedAt =
                        startedAtRef.current

                    if (
                        startedAt === null
                    ) {
                        return
                    }

                    const elapsed =
                        Date.now() -
                        startedAt

                    setDurationMs(
                        Math.min(
                            elapsed,
                            MAX_RECORDING_MS,
                        ),
                    )

                    if (
                        elapsed >=
                        MAX_RECORDING_MS
                    ) {
                        if (
                            recorder.state ===
                            "recording"
                        ) {
                            recorder.stop()
                        }
                    }
                }, 250)
        } catch (error) {
            console.error(
                "Could not access microphone",
                error,
            )

            setError(
                "Microphone access was denied or is unavailable.",
            )

            clearTimer()
            stopMediaTracks()
        }
    }

    function stopRecording() {
        const recorder =
            mediaRecorderRef.current

        if (
            !recorder ||
            recorder.state !==
            "recording"
        ) {
            return
        }

        recorder.stop()
    }

    function cancelRecording() {
        const recorder =
            mediaRecorderRef.current

        if (
            recorder &&
            recorder.state ===
            "recording"
        ) {
            recorder.onstop = null
            recorder.stop()
        }

        clearTimer()
        stopMediaTracks()

        mediaRecorderRef.current =
            null

        startedAtRef.current =
            null

        setRecording(false)

        resetRecording()
    }

    async function sendRecording() {
        if (
            !audioBlob ||
            durationMs <= 0 ||
            uploading
        ) {
            return
        }

        try {
            setUploading(true)
            setError(null)

            await onSend(
                audioBlob,
                durationMs,
            )

            resetRecording()
        } catch (error) {
            console.error(
                "Could not send voice message",
                error,
            )

            setError(
                "Could not send voice message.",
            )
        } finally {
            setUploading(false)
        }
    }

    useEffect(() => {
        return () => {
            clearTimer()

            const recorder =
                mediaRecorderRef.current

            if (
                recorder &&
                recorder.state ===
                "recording"
            ) {
                recorder.stop()
            }

            stopMediaTracks()
        }
    }, [])

    useEffect(() => {
        return () => {
            if (previewUrl) {
                URL.revokeObjectURL(
                    previewUrl,
                )
            }
        }
    }, [previewUrl])

    if (recording) {
        return (
            <div className="space-y-2">
                <div className="flex items-center gap-3">
                    <div className="flex items-center gap-2 text-sm">
                        <span className="h-2.5 w-2.5 animate-pulse rounded-full bg-destructive" />

                        Recording

                        <span className="font-mono">
                            {formatDuration(
                                durationMs,
                            )}
                        </span>
                    </div>

                    <Button
                        type="button"
                        size="icon"
                        variant="outline"
                        onClick={
                            stopRecording
                        }
                    >
                        <Square />
                    </Button>

                    <Button
                        type="button"
                        size="icon"
                        variant="ghost"
                        onClick={
                            cancelRecording
                        }
                    >
                        <X />
                    </Button>
                </div>

                {error && (
                    <p className="text-xs text-destructive">
                        {error}
                    </p>
                )}
            </div>
        )
    }

    if (
        audioBlob &&
        previewUrl
    ) {
        return (
            <div className="space-y-2">
                <div className="flex flex-wrap items-center gap-2">
                    <audio
                        controls
                        preload="metadata"
                        src={
                            previewUrl
                        }
                        className="max-w-72"
                    />

                    <span className="text-xs text-muted-foreground">
                        {formatDuration(
                            durationMs,
                        )}
                    </span>

                    <Button
                        type="button"
                        size="icon"
                        variant="ghost"
                        disabled={
                            uploading
                        }
                        onClick={
                            resetRecording
                        }
                    >
                        <X />
                    </Button>

                    <Button
                        type="button"
                        disabled={
                            uploading
                        }
                        onClick={() =>
                            void sendRecording()
                        }
                    >
                        {uploading ? (
                            <Loader2 className="animate-spin" />
                        ) : (
                            <Send />
                        )}

                        {uploading
                            ? "Sending..."
                            : "Send"}
                    </Button>
                </div>

                {error && (
                    <p className="text-xs text-destructive">
                        {error}
                    </p>
                )}
            </div>
        )
    }

    return (
        <div>
            <Button
                type="button"
                size="icon"
                variant="outline"
                disabled={
                    disabled ||
                    uploading
                }
                onClick={() =>
                    void startRecording()
                }
                title="Record voice message"
            >
                <Mic />
            </Button>

            {error && (
                <p className="mt-1 text-xs text-destructive">
                    {error}
                </p>
            )}
        </div>
    )
}