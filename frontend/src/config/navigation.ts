import {
    BookOpen,
    CircleUserRound,
    Home,
    MessageCircle,
    Trophy,
    UserRound,
    Users,
} from "lucide-react";

export const mainNavigation = [
    {
        title: "Dashboard",
        path: "/",
        icon: Home,
    },
    {
        title: "Profile",
        path: "/profile",
        icon: CircleUserRound,
    },
    {
        title: "Study Groups",
        path: "/groups",
        icon: Users,
    },
    {
        title: "Study Rooms",
        path: "/rooms",
        icon: BookOpen,
    },
    {
        title: "Friends",
        path: "/friends",
        icon: UserRound,
    },
    {
        title: "Challenges",
        path: "/challenges",
        icon: Trophy,
    },
    {
        title: "Messages",
        path: "/messages",
        icon: MessageCircle,
    },
] as const;