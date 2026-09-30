import { useState, type FormEvent } from "react";
import axios from "axios";
import { Link, useNavigate } from "react-router-dom";

import { authApi } from "@/api/authApi";

import {
    Alert,
    AlertDescription,
    AlertTitle,
} from "@/components/ui/alert";
import { Button } from "@/components/ui/button";
import {
    Card,
    CardContent,
    CardDescription,
    CardHeader,
    CardTitle,
} from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";

export default function RegisterPage() {
    const navigate = useNavigate();

    const [username, setUsername] = useState("");
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");

    const [error, setError] = useState<string | null>(null);
    const [isLoading, setIsLoading] = useState(false);

    async function handleSubmit(
        event: FormEvent<HTMLFormElement>,
    ) {
        event.preventDefault();

        setError(null);
        setIsLoading(true);

        try {
            await authApi.register({
                username,
                email,
                password,
            });

            navigate("/login", {
                replace: true,
            });
        } catch (error) {
            if (axios.isAxiosError(error)) {
                setError(
                    error.response?.data?.message ??
                    "Unable to create account. Please try again.",
                );
            } else {
                setError(
                    "Unable to create account. Please try again.",
                );
            }
        } finally {
            setIsLoading(false);
        }
    }

    return (
        <main className="flex min-h-screen items-center justify-center bg-muted/40 p-4">
            <Card className="w-full max-w-md">
                <CardHeader>
                    <CardTitle>Create an account</CardTitle>

                    <CardDescription>
                        Create your Group Study Platform account
                    </CardDescription>
                </CardHeader>

                <CardContent>
                    <form
                        onSubmit={handleSubmit}
                        className="space-y-4"
                    >
                        {error && (
                            <Alert variant="destructive">
                                <AlertTitle>
                                    Registration failed
                                </AlertTitle>

                                <AlertDescription>
                                    {error}
                                </AlertDescription>
                            </Alert>
                        )}

                        <div className="space-y-2">
                            <Label htmlFor="username">
                                Username
                            </Label>

                            <Input
                                id="username"
                                value={username}
                                onChange={(event) =>
                                    setUsername(event.target.value)
                                }
                                placeholder="hieu"
                                autoComplete="username"
                                minLength={3}
                                maxLength={50}
                                required
                            />
                        </div>

                        <div className="space-y-2">
                            <Label htmlFor="email">
                                Email
                            </Label>

                            <Input
                                id="email"
                                type="email"
                                value={email}
                                onChange={(event) =>
                                    setEmail(event.target.value)
                                }
                                placeholder="hieu@example.com"
                                autoComplete="email"
                                required
                            />
                        </div>

                        <div className="space-y-2">
                            <Label htmlFor="password">
                                Password
                            </Label>

                            <Input
                                id="password"
                                type="password"
                                value={password}
                                onChange={(event) =>
                                    setPassword(event.target.value)
                                }
                                placeholder="••••••••"
                                autoComplete="new-password"
                                minLength={8}
                                required
                            />
                        </div>

                        <Button
                            type="submit"
                            className="w-full"
                            disabled={isLoading}
                        >
                            {isLoading
                                ? "Creating account..."
                                : "Create account"}
                        </Button>

                        <p className="text-center text-sm text-muted-foreground">
                            Already have an account?{" "}
                            <Link
                                to="/login"
                                className="font-medium text-foreground underline underline-offset-4"
                            >
                                Sign in
                            </Link>
                        </p>
                    </form>
                </CardContent>
            </Card>
        </main>
    );
}