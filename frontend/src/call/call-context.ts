import {
    createContext,
    useContext,
} from "react"

import type { CallRoom } from "@/types/callRoom"

export interface ActiveCall {
    groupId: string
    callRoomId: string
    callRoomName: string
    serverUrl: string
    token: string
    roomName: string
    participantIdentity: string
}

export type CallViewMode =
    | "expanded"
    | "minimized"

export interface CallContextValue {
    activeCall: ActiveCall | null
    viewMode: CallViewMode
    joiningCallRoomId: string | null
    error: string | null

    joinCall: (
        groupId: string,
        callRoom: CallRoom,
    ) => Promise<boolean>

    minimizeCall: () => void
    expandCall: () => void
    leaveCall: () => void
    clearCallError: () => void
}

export const CallContext =
    createContext<CallContextValue | null>(
        null,
    )

export function useCall(): CallContextValue {
    const context =
        useContext(CallContext)

    if (!context) {
        throw new Error(
            "useCall must be used within CallProvider.",
        )
    }

    return context
}