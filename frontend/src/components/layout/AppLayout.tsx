import {
    Outlet,
    useLocation,
} from "react-router-dom"

import CallProvider from "@/call/CallProvider"
import GlobalCallOverlay from "@/call/GlobalCallOverlay"
import AppHeader from "@/components/layout/AppHeader"
import AppSidebar from "@/components/layout/AppSidebar"
import {
    SidebarInset,
    SidebarProvider,
} from "@/components/ui/sidebar"

export default function AppLayout() {
    const location =
        useLocation()

    const isStudyRooms =
        location.pathname ===
        "/rooms" ||
        location.pathname.startsWith(
            "/rooms/",
        )

    const isMessages =
        location.pathname ===
        "/messages" ||
        location.pathname.startsWith(
            "/messages/",
        )

    const isWorkspace =
        isStudyRooms ||
        isMessages

    return (
        <CallProvider>
            <SidebarProvider>
                <AppSidebar />

                <SidebarInset className="min-h-svh">
                    <AppHeader />

                    <main
                        className={
                            isWorkspace
                                ? "min-h-0 flex-1 overflow-hidden"
                                : "flex-1 p-6"
                        }
                    >
                        <Outlet />
                    </main>
                </SidebarInset>

                <GlobalCallOverlay />
            </SidebarProvider>
        </CallProvider>
    )
}