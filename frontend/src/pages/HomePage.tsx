import { useNavigate } from "react-router-dom";

import { useAuth } from "@/auth/useAuth";
import { Button } from "@/components/ui/button";
import {
    Card,
    CardContent,
    CardDescription,
    CardHeader,
    CardTitle,
} from "@/components/ui/card";

export default function HomePage() {
    const {
        user,
        logout,
    } = useAuth();

    const navigate = useNavigate();

    function handleLogout() {
        logout();

        navigate("/login", {
            replace: true,
        });
    }

    return (
        <main className="flex min-h-screen items-center justify-center bg-muted/40 p-4">
            <Card className="w-full max-w-lg">
                <CardHeader>
                    <CardTitle>
                        Welcome, {user?.username}
                    </CardTitle>

                    <CardDescription>
                        You are successfully authenticated.
                    </CardDescription>
                </CardHeader>

                <CardContent>
                    <Button
                        variant="outline"
                        onClick={handleLogout}
                    >
                        Log out
                    </Button>
                </CardContent>
            </Card>
        </main>
    );
}