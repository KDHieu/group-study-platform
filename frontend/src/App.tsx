import {
  Navigate,
  Route,
  Routes,
} from "react-router-dom";

import RegisterPage from "@/pages/RegisterPage";
import ProtectedRoute from "@/auth/ProtectedRoute";
import HomePage from "@/pages/HomePage";
import LoginPage from "@/pages/LoginPage";

function App() {
  return (
      <Routes>
        <Route
            path="/login"
            element={<LoginPage />}
        />

        <Route element={<ProtectedRoute />}>
          <Route
              path="/"
              element={<HomePage />}
          />
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