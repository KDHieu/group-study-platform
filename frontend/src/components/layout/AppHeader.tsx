import { LogOut, UserRound } from "lucide-react"
import { useNavigate } from "react-router-dom"

import { useAuth } from "@/auth/useAuth"
import { Avatar, AvatarFallback } from "@/components/ui/avatar"
import { Button } from "@/components/ui/button"
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuLabel,
    DropdownMenuSeparator,
    DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import { SidebarTrigger } from "@/components/ui/sidebar"

export default function AppHeader() {
    const { user, logout } = useAuth()
    const navigate = useNavigate()

    function handleLogout() {
        logout()

        navigate("/login", {
            replace: true,
        })
    }

    const initials =
        user?.username
            ?.slice(0, 2)
            .toUpperCase() ?? "US"

    return (
        <header className="flex h-16 items-center justify-between border-b px-4">
            <SidebarTrigger />

            <DropdownMenu>
                <DropdownMenuTrigger
                    render={
                        <Button
                            variant="ghost"
                            className="flex items-center gap-2"
                        />
                    }
                >
                    <Avatar className="h-8 w-8">
                        <AvatarFallback>
                            {initials}
                        </AvatarFallback>
                    </Avatar>

                    <span className="hidden sm:inline">
    {user?.username}
  </span>
                </DropdownMenuTrigger>

                <DropdownMenuContent
                    align="end"
                    className="w-52"
                >
                    <DropdownMenuLabel>
                        My account
                    </DropdownMenuLabel>

                    <DropdownMenuSeparator />

                    <DropdownMenuItem>
                        <UserRound />
                        Profile
                    </DropdownMenuItem>

                    <DropdownMenuItem
                        onClick={handleLogout}
                    >
                        <LogOut />
                        Log out
                    </DropdownMenuItem>
                </DropdownMenuContent>
            </DropdownMenu>
        </header>
    )
}