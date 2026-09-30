import {
    BookOpen,
    Flame,
    Plus,
    Trophy,
    Users,
} from "lucide-react"
import { Link } from "react-router-dom"

import { useAuth } from "@/auth/useAuth"
import { Button } from "@/components/ui/button"
import {
    Card,
    CardContent,
    CardDescription,
    CardHeader,
    CardTitle,
} from "@/components/ui/card"

const overviewItems = [
    {
        title: "Study Groups",
        value: "0",
        description: "Groups you have joined",
        icon: Users,
    },
    {
        title: "Study Rooms",
        value: "0",
        description: "Active study rooms",
        icon: BookOpen,
    },
    {
        title: "Study Streak",
        value: "0 days",
        description: "Keep learning every day",
        icon: Flame,
    },
    {
        title: "Challenges",
        value: "0",
        description: "Challenges completed",
        icon: Trophy,
    },
] as const

export default function DashboardPage() {
    const { user } = useAuth()

    return (
        <div className="space-y-8">
            <section>
                <h1 className="text-3xl font-semibold tracking-tight">
                    Welcome back, {user?.username}
                </h1>

                <p className="mt-2 text-muted-foreground">
                    Here&apos;s an overview of your learning activity.
                </p>
            </section>

            <section className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
                {overviewItems.map((item) => (
                    <Card key={item.title}>
                        <CardHeader className="flex flex-row items-center justify-between space-y-0">
                            <CardTitle className="text-sm font-medium">
                                {item.title}
                            </CardTitle>

                            <item.icon className="h-4 w-4 text-muted-foreground" />
                        </CardHeader>

                        <CardContent>
                            <div className="text-2xl font-bold">
                                {item.value}
                            </div>

                            <p className="text-xs text-muted-foreground">
                                {item.description}
                            </p>
                        </CardContent>
                    </Card>
                ))}
            </section>

            <section className="grid gap-6 lg:grid-cols-2">
                <Card>
                    <CardHeader>
                        <CardTitle>Study Groups</CardTitle>

                        <CardDescription>
                            Create or join a group to start studying
                            with others.
                        </CardDescription>
                    </CardHeader>

                    <CardContent className="flex gap-3">
                        <Button render={<Link to="/groups" />}>
                            <Users />
                            Browse groups
                        </Button>

                        <Button
                            variant="outline"
                            render={<Link to="/groups" />}
                        >
                            <Plus />
                            Create group
                        </Button>
                    </CardContent>
                </Card>

                <Card>
                    <CardHeader>
                        <CardTitle>Daily Challenge</CardTitle>

                        <CardDescription>
                            Complete daily challenges to maintain your
                            learning streak.
                        </CardDescription>
                    </CardHeader>

                    <CardContent>
                        <Button
                            variant="outline"
                            render={<Link to="/challenges" />}
                        >
                            <Trophy />
                            View challenges
                        </Button>
                    </CardContent>
                </Card>
            </section>
        </div>
    )
}