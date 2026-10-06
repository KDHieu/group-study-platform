import {
    Navigate,
    Route,
    Routes,
    useParams,
} from "react-router-dom"

import ProtectedRoute from "@/auth/ProtectedRoute"
import AppLayout from "@/components/layout/AppLayout"
import ChallengesPage from "@/pages/ChallengesPage"
import DashboardPage from "@/pages/DashboardPage"
import FriendsPage from "@/pages/FriendsPage"
import GroupDetailPage from "@/pages/GroupDetailPage"
import GroupsPage from "@/pages/GroupsPage"
import LoginPage from "@/pages/LoginPage"
import MessagesPage from "@/pages/MessagesPage"
import ProfilePage from "@/pages/ProfilePage"
import RegisterPage from "@/pages/RegisterPage"
import StudyRoomsPage from "@/pages/StudyRoomsPage"

function LegacyStudyRoomRedirect() {
    const {
        groupId,
    } =
        useParams()

    if (!groupId) {
        return (
            <Navigate
                to="/rooms"
                replace
            />
        )
    }

    return (
        <Navigate
            to={`/rooms/${groupId}`}
            replace
        />
    )
}

function App() {
    return (
        <Routes>
            <Route
                path="/login"
                element={
                    <LoginPage />
                }
            />

            <Route
                path="/register"
                element={
                    <RegisterPage />
                }
            />

            <Route
                element={
                    <ProtectedRoute />
                }
            >
                <Route
                    element={
                        <AppLayout />
                    }
                >
                    <Route
                        path="/"
                        element={
                            <DashboardPage />
                        }
                    />

                    <Route
                        path="/profile"
                        element={
                            <ProfilePage />
                        }
                    />

                    <Route
                        path="/groups"
                        element={
                            <GroupsPage />
                        }
                    />

                    <Route
                        path="/groups/:groupId"
                        element={
                            <GroupDetailPage />
                        }
                    />

                    <Route
                        path="/rooms"
                        element={
                            <StudyRoomsPage />
                        }
                    />

                    <Route
                        path="/rooms/:groupId"
                        element={
                            <StudyRoomsPage />
                        }
                    />

                    <Route
                        path="/groups/:groupId/room"
                        element={
                            <LegacyStudyRoomRedirect />
                        }
                    />

                    <Route
                        path="/groups/:groupId/room/chat"
                        element={
                            <LegacyStudyRoomRedirect />
                        }
                    />

                    <Route
                        path="/groups/:groupId/room/video"
                        element={
                            <LegacyStudyRoomRedirect />
                        }
                    />

                    <Route
                        path="/groups/:groupId/room/members"
                        element={
                            <LegacyStudyRoomRedirect />
                        }
                    />

                    <Route
                        path="/friends"
                        element={
                            <FriendsPage />
                        }
                    />

                    <Route
                        path="/messages"
                        element={
                            <MessagesPage />
                        }
                    />

                    <Route
                        path="/messages/:userId"
                        element={
                            <MessagesPage />
                        }
                    />

                    <Route
                        path="/challenges"
                        element={
                            <ChallengesPage />
                        }
                    />
                </Route>
            </Route>

            <Route
                path="*"
                element={
                    <Navigate
                        to="/"
                        replace
                    />
                }
            />
        </Routes>
    )
}

export default App