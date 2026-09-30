import {
  Navigate,
  Route,
  Routes,
} from "react-router-dom";

import AppLayout from "@/components/layout/AppLayout"
import RegisterPage from "@/pages/RegisterPage";
import ProtectedRoute from "@/auth/ProtectedRoute";
import DashboardPage from "@/pages/DashboardPage"
import LoginPage from "@/pages/LoginPage";

function App() {
  return (
      <Routes>
        <Route
            path="/login"
            element={<LoginPage />}
        />

          <Route element={<ProtectedRoute />}>
              <Route element={<AppLayout />}>
                  <Route
                      path="/"
                      element={<DashboardPage />}
                  />

                  <Route
                      path="/groups"
                      element={<div>Study Groups</div>}
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

        <Route
            path="/register"
            element={<RegisterPage />}
        />
      </Routes>
  );
}

export default App;