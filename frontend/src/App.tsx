import {
    Navigate,
    Route,
    Routes,
} from "react-router-dom";

import ProtectedRoute from "@/auth/ProtectedRoute";
import AppLayout from "@/components/layout/AppLayout";
import DashboardPage from "@/pages/DashboardPage";
import GroupDetailPage from "@/pages/GroupDetailPage";
import GroupsPage from "@/pages/GroupsPage";
import LoginPage from "@/pages/LoginPage";
import ProfilePage from "@/pages/ProfilePage";
import RegisterPage from "@/pages/RegisterPage";

function App() {
    return (
        <Routes>
            <Route
                path="/login"
                element={<LoginPage />}
            />

            <Route
                path="/register"
                element={<RegisterPage />}
            />

            <Route element={<ProtectedRoute />}>
                <Route element={<AppLayout />}>
                    <Route
                        path="/"
                        element={<DashboardPage />}
                    />

                    <Route
                        path="/profile"
                        element={<ProfilePage />}
                    />

                    <Route
                        path="/groups"
                        element={<GroupsPage />}
                    />

                    <Route
                        path="/groups/:groupId"
                        element={<GroupDetailPage />}
                    />

                    <Route
                        path="/rooms"
                        element={<div>Study Rooms</div>}
                    />

                    <Route
                        path="/friends"
                        element={<div>Friends</div>}
                    />

                    <Route
                        path="/challenges"
                        element={<div>Challenges</div>}
                    />

                    <Route
                        path="/messages"
                        element={<div>Messages</div>}
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
    );
}

export default App;