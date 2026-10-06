import {
    useRef,
    useState,
} from "react"
import type {
    ReactNode,
} from "react"

import { getApiErrorMessage } from "@/api/error"
import { videoApi } from "@/api/videoApi"
import {
    CallContext,
    type ActiveCall,
    type CallViewMode,
} from "@/call/call-context"
import type { CallRoom } from "@/types/callRoom"

interface CallProviderProps {
    children: ReactNode
}

export default function CallProvider({
                                         children,
                                     }: CallProviderProps) {
    const [
        activeCall,
        setActiveCall,
    ] =
        useState<ActiveCall | null>(
            null,
        )

    const [
        viewMode,
        setViewMode,
    ] =
        useState<CallViewMode>(
            "expanded",
        )

    const [
        joiningCallRoomId,
        setJoiningCallRoomId,
    ] =
        useState<string | null>(
            null,
        )

    const [
        error,
        setError,
    ] =
        useState<string | null>(
            null,
        )

    /*
     * Used to ignore a stale async token response if the
     * user starts another join request before the first
     * request completes.
     */
    const joinRequestIdRef =
        useRef(0)

    async function joinCall(
        groupId: string,
        callRoom: CallRoom,
    ): Promise<boolean> {
        /*
         * Already connected to this logical call room.
         * No new token request is necessary.
         */
        if (
            activeCall?.groupId ===
            groupId &&
            activeCall.callRoomId ===
            callRoom.id
        ) {
            setViewMode("expanded")
            setError(null)

            return true
        }

        const currentGroupId =
            groupId

        const currentCallRoomId =
            callRoom.id

        const currentCallRoomName =
            callRoom.name

        const requestId =
            joinRequestIdRef.current + 1

        joinRequestIdRef.current =
            requestId

        setJoiningCallRoomId(
            currentCallRoomId,
        )

        setError(null)

        try {
            const session =
                await videoApi
                    .createCallRoomJoinToken(
                        currentGroupId,
                        currentCallRoomId,
                    )

            /*
             * Another join request may have started while
             * this request was waiting for the backend.
             */
            if (
                joinRequestIdRef.current !==
                requestId
            ) {
                return false
            }

            setActiveCall({
                groupId:
                currentGroupId,

                callRoomId:
                currentCallRoomId,

                callRoomName:
                currentCallRoomName,

                serverUrl:
                session.serverUrl,

                token:
                session.token,

                roomName:
                session.roomName,

                participantIdentity:
                session.participantIdentity,
            })

            setViewMode(
                "expanded",
            )

            return true
        } catch (error) {
            if (
                joinRequestIdRef.current !==
                requestId
            ) {
                return false
            }

            setError(
                getApiErrorMessage(
                    error,
                    "Could not join the call room.",
                ),
            )

            return false
        } finally {
            if (
                joinRequestIdRef.current ===
                requestId
            ) {
                setJoiningCallRoomId(
                    null,
                )
            }
        }
    }

    function minimizeCall() {
        if (!activeCall) {
            return
        }

        setViewMode(
            "minimized",
        )
    }

    function expandCall() {
        if (!activeCall) {
            return
        }

        setViewMode(
            "expanded",
        )
    }

    function leaveCall() {
        /*
         * Invalidate any token request that may still be
         * running before clearing the active call.
         */
        joinRequestIdRef.current +=
            1

        setActiveCall(null)

        setJoiningCallRoomId(
            null,
        )

        setViewMode(
            "expanded",
        )

        setError(null)
    }

    function clearCallError() {
        setError(null)
    }

    return (
        <CallContext.Provider
            value={{
                activeCall,
                viewMode,
                joiningCallRoomId,
                error,
                joinCall,
                minimizeCall,
                expandCall,
                leaveCall,
                clearCallError,
            }}
        >
            {children}
        </CallContext.Provider>
    )
}