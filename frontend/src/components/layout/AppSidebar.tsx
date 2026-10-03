import {
    ChevronsUpDown,
    LogOut,
    UserRound,
} from "lucide-react"
import {
    NavLink,
    useLocation,
    useNavigate,
} from "react-router-dom"

import { useAuth } from "@/auth/useAuth"
import { mainNavigation } from "@/config/navigation"
import {
    Avatar,
    AvatarFallback,
} from "@/components/ui/avatar"
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuGroup,
    DropdownMenuItem,
    DropdownMenuLabel,
    DropdownMenuSeparator,
    DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import {
    Sidebar,
    SidebarContent,
    SidebarFooter,
    SidebarGroup,
    SidebarGroupContent,
    SidebarGroupLabel,
    SidebarMenu,
    SidebarMenuButton,
    SidebarMenuItem,
    SidebarRail,
} from "@/components/ui/sidebar"

export default function AppSidebar() {
    const location = useLocation()
    const navigate = useNavigate()

    const { user, logout } = useAuth()

    const initials =
        user?.username
            ?.slice(0, 2)
            .toUpperCase() ?? "US"

    function handleProfile() {
        navigate("/profile")
    }

    function handleLogout() {
        logout()

        navigate("/login", {
            replace: true,
        })
    }

    return (
        <Sidebar collapsible="icon">
            <SidebarContent>
                <SidebarGroup>
                    <SidebarGroupLabel>
                        Group Study
                    </SidebarGroupLabel>

                    <SidebarGroupContent>
                        <SidebarMenu>
                            {mainNavigation.map((item) => {
                                const isActive =
                                    item.path === "/"
                                        ? location.pathname === "/"
                                        : location.pathname.startsWith(
                                            item.path,
                                        )

                                return (
                                    <SidebarMenuItem
                                        key={item.path}
                                    >
                                        <SidebarMenuButton
                                            isActive={isActive}
                                            tooltip={item.title}
                                            render={
                                                <NavLink
                                                    to={item.path}
                                                />
                                            }
                                        >
                                            <item.icon />

                                            <span>
                                                {item.title}
                                            </span>
                                        </SidebarMenuButton>
                                    </SidebarMenuItem>
                                )
                            })}
                        </SidebarMenu>
                    </SidebarGroupContent>
                </SidebarGroup>
            </SidebarContent>

            <SidebarFooter>
                <SidebarMenu>
                    <SidebarMenuItem>
                        <DropdownMenu>
                            <DropdownMenuTrigger
                                className="
                                    flex h-12 w-full items-center gap-2
                                    overflow-hidden rounded-md p-2
                                    text-left text-sm
                                    outline-none
                                    transition-colors
                                    hover:bg-sidebar-accent
                                    hover:text-sidebar-accent-foreground
                                    focus-visible:ring-2
                                    focus-visible:ring-sidebar-ring
                                    data-popup-open:bg-sidebar-accent
                                    data-popup-open:text-sidebar-accent-foreground
                                    group-data-[collapsible=icon]:size-8
                                    group-data-[collapsible=icon]:p-0
                                "
                                title={
                                    user?.username ??
                                    "My account"
                                }
                            >
                                <Avatar className="size-8 shrink-0">
                                    <AvatarFallback>
                                        {initials}
                                    </AvatarFallback>
                                </Avatar>

                                <div className="grid min-w-0 flex-1 text-left text-sm leading-tight group-data-[collapsible=icon]:hidden">
                                    <span className="truncate font-medium">
                                        {user?.username ??
                                            "User"}
                                    </span>

                                    <span className="truncate text-xs text-muted-foreground">
                                        My account
                                    </span>
                                </div>

                                <ChevronsUpDown className="ml-auto size-4 shrink-0 group-data-[collapsible=icon]:hidden" />
                            </DropdownMenuTrigger>

                            <DropdownMenuContent
                                side="top"
                                align="start"
                                sideOffset={8}
                                className="min-w-56"
                            >
                                <DropdownMenuGroup>
                                    <DropdownMenuLabel>
                                        <div className="flex items-center gap-2 py-1">
                                            <Avatar className="size-8">
                                                <AvatarFallback>
                                                    {initials}
                                                </AvatarFallback>
                                            </Avatar>

                                            <div className="grid min-w-0 text-sm leading-tight">
                                                <span className="truncate font-medium">
                                                    {user?.username ??
                                                        "User"}
                                                </span>

                                                <span className="text-xs text-muted-foreground">
                                                    My account
                                                </span>
                                            </div>
                                        </div>
                                    </DropdownMenuLabel>
                                </DropdownMenuGroup>

                                <DropdownMenuSeparator />

                                <DropdownMenuItem
                                    onClick={handleProfile}
                                >
                                    <UserRound />
                                    Profile
                                </DropdownMenuItem>

                                <DropdownMenuSeparator />

                                <DropdownMenuItem
                                    variant="destructive"
                                    onClick={handleLogout}
                                >
                                    <LogOut />
                                    Log out
                                </DropdownMenuItem>
                            </DropdownMenuContent>
                        </DropdownMenu>
                    </SidebarMenuItem>
                </SidebarMenu>
            </SidebarFooter>

            <SidebarRail />
        </Sidebar>
    )
}