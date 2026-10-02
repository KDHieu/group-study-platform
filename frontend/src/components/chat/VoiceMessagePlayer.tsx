import {
    useEffect,
    useRef,
    useState,
} from "react"
import {
    AudioLines,
    Loader2,
    Pause,
    Play,
} from "lucide-react"

import { chatApi } from "@/api/chatApi"
import { Button } from "@/components/ui/button"

interface VoiceMessagePlayerProps {
    mediaUrl: string
    durationMs: number | null
}

function formatTime(
    seconds: number,
): string {
    if (
        !Number.isFinite(seconds) ||
        seconds < 0
    ) {
        return "0:00"
    }

    const roundedSeconds =
        Math.floor(seconds)

    const minutes =
        Math.floor(
            roundedSeconds / 60,
        )

    const remainingSeconds =
        roundedSeconds % 60

    return `${minutes}:${remainingSeconds
        .toString()
        .padStart(2, "0")}`
}

export default function VoiceMessagePlayer({
                                               mediaUrl,
                                               durationMs,
                                           }: VoiceMessagePlayerProps) {
    const audioRef =
        useRef<HTMLAudioElement | null>(
            null,
        )

    const [audioUrl, setAudioUrl] =
        useState<string | null>(null)

    const [loading, setLoading] =
        useState(true)

    const [error, setError] =
        useState(false)

    const [playing, setPlaying] =
        useState(false)

    const [currentTime, setCurrentTime] =
        useState(0)

    const fallbackDuration =
        durationMs
            ? durationMs / 1000
            : 0

    const [duration, setDuration] =
        useState(
            fallbackDuration,
        )

    useEffect(() => {
        let objectUrl: string | null =
            null

        let cancelled = false

        async function loadAudio() {
            try {
                setLoading(true)
                setError(false)

                const blob =
                    await chatApi.getAudioBlob(
                        mediaUrl,
                    )

                if (cancelled) {
                    return
                }

                objectUrl =
                    URL.createObjectURL(
                        blob,
                    )

                setAudioUrl(objectUrl)
            } catch {
                if (!cancelled) {
                    setError(true)
                }
            } finally {
                if (!cancelled) {
                    setLoading(false)
                }
            }
        }

        void loadAudio()

        return () => {
            cancelled = true

            if (objectUrl) {
                URL.revokeObjectURL(
                    objectUrl,
                )
            }
        }
    }, [mediaUrl])

    async function togglePlayback() {
        const audio =
            audioRef.current

        if (!audio) {
            return
        }

        if (audio.paused) {
            try {
                await audio.play()
            } catch {
                setError(true)
            }
        } else {
            audio.pause()
        }
    }

    function handleLoadedMetadata() {
        const audio =
            audioRef.current

        if (!audio) {
            return
        }

        if (
            Number.isFinite(
                audio.duration,
            ) &&
            audio.duration > 0
        ) {
            setDuration(
                audio.duration,
            )
        }
    }

    function handleProgressChange(
        value: number,
    ) {
        const audio =
            audioRef.current

        if (!audio) {
            return
        }

        audio.currentTime =
            value

        setCurrentTime(value)
    }

    if (loading) {
        return (
            <div className="flex min-w-56 items-center gap-2 py-1">
                <Loader2 className="h-4 w-4 animate-spin" />

                <span className="text-sm">
                    Loading audio...
                </span>
            </div>
        )
    }

    if (
        error ||
        !audioUrl
    ) {
        return (
            <span className="text-sm">
                Unable to load voice message.
            </span>
        )
    }

    return (
        <div className="flex min-w-[250px] max-w-[320px] items-center gap-3">
            <audio
                ref={audioRef}
                src={audioUrl}
                preload="metadata"
                className="hidden"
                onLoadedMetadata={
                    handleLoadedMetadata
                }
                onPlay={() =>
                    setPlaying(true)
                }
                onPause={() =>
                    setPlaying(false)
                }
                onTimeUpdate={() => {
                    const audio =
                        audioRef.current

                    if (audio) {
                        setCurrentTime(
                            audio.currentTime,
                        )
                    }
                }}
                onEnded={() => {
                    setPlaying(false)
                    setCurrentTime(0)
                }}
            />

            <Button
                type="button"
                size="icon"
                variant="secondary"
                className="h-10 w-10 shrink-0 rounded-full"
                onClick={() =>
                    void togglePlayback()
                }
            >
                {playing ? (
                    <Pause className="h-4 w-4" />
                ) : (
                    <Play className="h-4 w-4" />
                )}
            </Button>

            <div className="min-w-0 flex-1 space-y-1">
                <div className="flex items-center gap-2">
                    <AudioLines className="h-4 w-4 shrink-0 opacity-70" />

                    <input
                        type="range"
                        min={0}
                        max={
                            duration > 0
                                ? duration
                                : 1
                        }
                        step={0.01}
                        value={
                            currentTime
                        }
                        onChange={(
                            event,
                        ) =>
                            handleProgressChange(
                                Number(
                                    event
                                        .target
                                        .value,
                                ),
                            )
                        }
                        className="h-1.5 w-full cursor-pointer accent-current"
                    />
                </div>

                <div className="flex justify-between text-[11px] opacity-70">
                    <span>
                        {formatTime(
                            currentTime,
                        )}
                    </span>

                    <span>
                        {formatTime(
                            duration,
                        )}
                    </span>
                </div>
            </div>
        </div>
    )
}