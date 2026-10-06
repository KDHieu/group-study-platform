import {
    CalendarDays,
    CheckCircle2,
    CircleHelp,
    Flame,
    History,
    Loader2,
    Trophy,
    XCircle,
} from "lucide-react"
import {
    useEffect,
    useState,
} from "react"

import {
    challengeApi,
} from "@/api/challengeApi"
import {
    getApiErrorMessage,
} from "@/api/error"
import {
    Button,
} from "@/components/ui/button"
import {
    Card,
    CardContent,
    CardDescription,
    CardHeader,
    CardTitle,
} from "@/components/ui/card"
import type {
    ChallengeHistoryItem,
    ChallengeStreak,
    TodayChallenge,
} from "@/types/challenge"

function formatChallengeDate(
    value: string,
): string {
    return new Date(
        `${value}T00:00:00`,
    ).toLocaleDateString(
        [],
        {
            weekday: "short",
            year: "numeric",
            month: "short",
            day: "numeric",
        },
    )
}

function optionClassName(
    optionKey: string,
    challenge: TodayChallenge,
    selectedOption: string,
): string {
    const base =
        "flex w-full items-start gap-3 rounded-xl border p-4 text-left transition-colors"

    if (challenge.completed) {
        if (
            optionKey ===
            challenge.correctOption
        ) {
            return `${base} border-green-500 bg-green-500/10`
        }

        if (
            optionKey ===
            challenge.selectedOption &&
            challenge.correct ===
            false
        ) {
            return `${base} border-destructive bg-destructive/10`
        }

        return `${base} opacity-70`
    }

    if (
        optionKey ===
        selectedOption
    ) {
        return `${base} border-primary bg-primary/10`
    }

    return `${base} hover:bg-muted/60`
}

export default function ChallengesPage() {
    const [
        challenge,
        setChallenge,
    ] =
        useState<TodayChallenge | null>(
            null,
        )

    const [
        streak,
        setStreak,
    ] =
        useState<ChallengeStreak | null>(
            null,
        )

    const [
        history,
        setHistory,
    ] =
        useState<
            ChallengeHistoryItem[]
        >([])

    const [
        selectedOption,
        setSelectedOption,
    ] =
        useState("")

    const [
        historyPage,
        setHistoryPage,
    ] =
        useState(0)

    const [
        historyTotalPages,
        setHistoryTotalPages,
    ] =
        useState(0)

    const [
        loading,
        setLoading,
    ] =
        useState(true)

    const [
        historyLoading,
        setHistoryLoading,
    ] =
        useState(true)

    const [
        submitting,
        setSubmitting,
    ] =
        useState(false)

    const [
        error,
        setError,
    ] =
        useState<string | null>(
            null,
        )

    useEffect(() => {
        let cancelled =
            false

        async function loadOverview() {
            try {
                setLoading(true)
                setError(null)

                const [
                    todayResponse,
                    streakResponse,
                ] =
                    await Promise.all([
                        challengeApi
                            .getToday(),

                        challengeApi
                            .getStreak(),
                    ])

                if (cancelled) {
                    return
                }

                setChallenge(
                    todayResponse,
                )

                setStreak(
                    streakResponse,
                )

                setSelectedOption(
                    todayResponse
                        .selectedOption ??
                    "",
                )
            } catch (
                requestError
                ) {
                if (
                    !cancelled
                ) {
                    setError(
                        getApiErrorMessage(
                            requestError,
                            "Could not load today's challenge.",
                        ),
                    )
                }
            } finally {
                if (
                    !cancelled
                ) {
                    setLoading(
                        false,
                    )
                }
            }
        }

        void loadOverview()

        return () => {
            cancelled = true
        }
    }, [])

    useEffect(() => {
        const currentPage =
            historyPage

        let cancelled =
            false

        async function loadHistory() {
            try {
                setHistoryLoading(
                    true,
                )

                const response =
                    await challengeApi
                        .getHistory(
                            currentPage,
                            10,
                        )

                if (cancelled) {
                    return
                }

                setHistory(
                    response.content,
                )

                setHistoryTotalPages(
                    response.page
                        .totalPages,
                )
            } catch (
                requestError
                ) {
                if (
                    !cancelled
                ) {
                    setError(
                        getApiErrorMessage(
                            requestError,
                            "Could not load challenge history.",
                        ),
                    )
                }
            } finally {
                if (
                    !cancelled
                ) {
                    setHistoryLoading(
                        false,
                    )
                }
            }
        }

        void loadHistory()

        return () => {
            cancelled = true
        }
    }, [
        historyPage,
    ])

    async function handleSubmit() {
        if (
            !selectedOption ||
            submitting
        ) {
            return
        }

        try {
            setSubmitting(
                true,
            )

            setError(null)

            const response =
                await challengeApi
                    .submitToday(
                        selectedOption,
                    )

            setChallenge(
                response,
            )

            const [
                streakResponse,
                historyResponse,
            ] =
                await Promise.all([
                    challengeApi
                        .getStreak(),

                    challengeApi
                        .getHistory(
                            0,
                            10,
                        ),
                ])

            setStreak(
                streakResponse,
            )

            setHistory(
                historyResponse.content,
            )

            setHistoryPage(
                0,
            )

            setHistoryTotalPages(
                historyResponse.page
                    .totalPages,
            )
        } catch (
            requestError
            ) {
            setError(
                getApiErrorMessage(
                    requestError,
                    "Could not submit your answer.",
                ),
            )
        } finally {
            setSubmitting(
                false,
            )
        }
    }

    if (loading) {
        return (
            <div className="flex min-h-[420px] items-center justify-center">
                <div className="flex items-center gap-2 text-muted-foreground">
                    <Loader2 className="size-5 animate-spin" />

                    Loading daily challenge...
                </div>
            </div>
        )
    }

    return (
        <div className="mx-auto max-w-6xl space-y-6">
            <div className="flex flex-col gap-2 sm:flex-row sm:items-end sm:justify-between">
                <div>
                    <h1 className="text-3xl font-semibold tracking-tight">
                        Daily Challenge
                    </h1>

                    <p className="mt-1 text-muted-foreground">
                        Solve one challenge every day and keep your learning streak alive.
                    </p>
                </div>

                {challenge && (
                    <div className="flex items-center gap-2 text-sm text-muted-foreground">
                        <CalendarDays className="size-4" />

                        {formatChallengeDate(
                            challenge.challengeDate,
                        )}
                    </div>
                )}
            </div>

            {error && (
                <div className="rounded-lg border border-destructive/30 bg-destructive/5 px-4 py-3 text-sm text-destructive">
                    {error}
                </div>
            )}

            <div className="grid gap-4 sm:grid-cols-3">
                <Card>
                    <CardContent className="flex items-center gap-4 p-5">
                        <div className="flex size-11 items-center justify-center rounded-full bg-orange-500/10">
                            <Flame className="size-5 text-orange-500" />
                        </div>

                        <div>
                            <p className="text-sm text-muted-foreground">
                                Current streak
                            </p>

                            <p className="text-2xl font-semibold">
                                {streak?.currentStreak ??
                                    0}
                            </p>
                        </div>
                    </CardContent>
                </Card>

                <Card>
                    <CardContent className="flex items-center gap-4 p-5">
                        <div className="flex size-11 items-center justify-center rounded-full bg-yellow-500/10">
                            <Trophy className="size-5 text-yellow-500" />
                        </div>

                        <div>
                            <p className="text-sm text-muted-foreground">
                                Longest streak
                            </p>

                            <p className="text-2xl font-semibold">
                                {streak?.longestStreak ??
                                    0}
                            </p>
                        </div>
                    </CardContent>
                </Card>

                <Card>
                    <CardContent className="flex items-center gap-4 p-5">
                        <div className="flex size-11 items-center justify-center rounded-full bg-primary/10">
                            <CheckCircle2 className="size-5 text-primary" />
                        </div>

                        <div>
                            <p className="text-sm text-muted-foreground">
                                Completed
                            </p>

                            <p className="text-2xl font-semibold">
                                {streak?.completedChallenges ??
                                    0}
                            </p>
                        </div>
                    </CardContent>
                </Card>
            </div>

            {challenge && (
                <Card>
                    <CardHeader>
                        <div className="flex items-start justify-between gap-4">
                            <div>
                                <CardTitle className="flex items-center gap-2">
                                    <CircleHelp className="size-5" />

                                    Today&apos;s question
                                </CardTitle>

                                <CardDescription className="mt-2">
                                    Choose the best answer. You can submit only once today.
                                </CardDescription>
                            </div>

                            {challenge.completed && (
                                <div
                                    className={`rounded-full px-3 py-1 text-xs font-medium ${
                                        challenge.correct
                                            ? "bg-green-500/10 text-green-600"
                                            : "bg-destructive/10 text-destructive"
                                    }`}
                                >
                                    {challenge.correct
                                        ? "Correct"
                                        : "Incorrect"}
                                </div>
                            )}
                        </div>
                    </CardHeader>

                    <CardContent className="space-y-5">
                        <p className="text-lg font-medium leading-relaxed">
                            {challenge.question}
                        </p>

                        <div className="grid gap-3">
                            {challenge.options.map(
                                (
                                    option,
                                ) => (
                                    <button
                                        key={
                                            option.key
                                        }
                                        type="button"
                                        disabled={
                                            challenge.completed
                                        }
                                        onClick={() =>
                                            setSelectedOption(
                                                option.key,
                                            )
                                        }
                                        className={optionClassName(
                                            option.key,
                                            challenge,
                                            selectedOption,
                                        )}
                                    >
                                        <span className="flex size-8 shrink-0 items-center justify-center rounded-full border bg-background text-sm font-semibold">
                                            {
                                                option.key
                                            }
                                        </span>

                                        <span className="pt-1 text-sm">
                                            {
                                                option.text
                                            }
                                        </span>

                                        {challenge.completed &&
                                            option.key ===
                                            challenge.correctOption && (
                                                <CheckCircle2 className="ml-auto mt-1 size-5 shrink-0 text-green-600" />
                                            )}

                                        {challenge.completed &&
                                            challenge.correct ===
                                            false &&
                                            option.key ===
                                            challenge.selectedOption && (
                                                <XCircle className="ml-auto mt-1 size-5 shrink-0 text-destructive" />
                                            )}
                                    </button>
                                ),
                            )}
                        </div>

                        {!challenge.completed ? (
                            <div className="flex justify-end">
                                <Button
                                    disabled={
                                        !selectedOption ||
                                        submitting
                                    }
                                    onClick={() =>
                                        void handleSubmit()
                                    }
                                >
                                    {submitting ? (
                                        <>
                                            <Loader2 className="animate-spin" />

                                            Submitting...
                                        </>
                                    ) : (
                                        "Submit answer"
                                    )}
                                </Button>
                            </div>
                        ) : (
                            <div
                                className={`rounded-xl border p-4 ${
                                    challenge.correct
                                        ? "border-green-500/30 bg-green-500/5"
                                        : "border-destructive/30 bg-destructive/5"
                                }`}
                            >
                                <div className="flex items-start gap-3">
                                    {challenge.correct ? (
                                        <CheckCircle2 className="mt-0.5 size-5 shrink-0 text-green-600" />
                                    ) : (
                                        <XCircle className="mt-0.5 size-5 shrink-0 text-destructive" />
                                    )}

                                    <div className="space-y-1">
                                        <p className="font-medium">
                                            {challenge.correct
                                                ? "Correct answer!"
                                                : `Correct answer: ${challenge.correctOption}`}
                                        </p>

                                        <p className="text-sm text-muted-foreground">
                                            {
                                                challenge.explanation
                                            }
                                        </p>
                                    </div>
                                </div>
                            </div>
                        )}
                    </CardContent>
                </Card>
            )}

            <Card>
                <CardHeader>
                    <CardTitle className="flex items-center gap-2">
                        <History className="size-5" />

                        Challenge history
                    </CardTitle>

                    <CardDescription>
                        Review the challenges you have completed.
                    </CardDescription>
                </CardHeader>

                <CardContent>
                    {historyLoading ? (
                        <div className="flex items-center justify-center py-10 text-sm text-muted-foreground">
                            <Loader2 className="mr-2 size-4 animate-spin" />

                            Loading history...
                        </div>
                    ) : history.length ===
                    0 ? (
                        <div className="py-10 text-center text-sm text-muted-foreground">
                            Complete your first daily challenge to start building history.
                        </div>
                    ) : (
                        <div className="space-y-3">
                            {history.map(
                                (
                                    item,
                                ) => (
                                    <div
                                        key={
                                            item.challengeId
                                        }
                                        className="flex flex-col gap-3 rounded-xl border p-4 sm:flex-row sm:items-center sm:justify-between"
                                    >
                                        <div className="min-w-0">
                                            <div className="flex items-center gap-2">
                                                {item.correct ? (
                                                    <CheckCircle2 className="size-4 shrink-0 text-green-600" />
                                                ) : (
                                                    <XCircle className="size-4 shrink-0 text-destructive" />
                                                )}

                                                <p className="text-sm font-medium">
                                                    {formatChallengeDate(
                                                        item.challengeDate,
                                                    )}
                                                </p>
                                            </div>

                                            <p className="mt-2 line-clamp-2 text-sm text-muted-foreground">
                                                {
                                                    item.question
                                                }
                                            </p>
                                        </div>

                                        <div className="shrink-0 text-sm">
                                            <p>
                                                Your answer:{" "}
                                                <span className="font-medium">
                                                    {
                                                        item.selectedOption
                                                    }
                                                </span>
                                            </p>

                                            {!item.correct && (
                                                <p className="text-muted-foreground">
                                                    Correct:{" "}
                                                    <span className="font-medium text-foreground">
                                                        {
                                                            item.correctOption
                                                        }
                                                    </span>
                                                </p>
                                            )}
                                        </div>
                                    </div>
                                ),
                            )}

                            {historyTotalPages >
                                1 && (
                                    <div className="flex items-center justify-between pt-3">
                                        <Button
                                            variant="outline"
                                            disabled={
                                                historyPage ===
                                                0
                                            }
                                            onClick={() =>
                                                setHistoryPage(
                                                    (
                                                        current,
                                                    ) =>
                                                        Math.max(
                                                            0,
                                                            current -
                                                            1,
                                                        ),
                                                )
                                            }
                                        >
                                            Previous
                                        </Button>

                                        <span className="text-sm text-muted-foreground">
                                        Page{" "}
                                            {historyPage +
                                                1}{" "}
                                            of{" "}
                                            {
                                                historyTotalPages
                                            }
                                    </span>

                                        <Button
                                            variant="outline"
                                            disabled={
                                                historyPage +
                                                1 >=
                                                historyTotalPages
                                            }
                                            onClick={() =>
                                                setHistoryPage(
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
                    )}
                </CardContent>
            </Card>
        </div>
    )
}