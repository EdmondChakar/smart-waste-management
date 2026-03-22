import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom";
import LoginPage from "./pages/auth/LoginPage";
import SignupPage from "./pages/auth/SignupPage";
import { APP_ROUTES } from "./constants/routes";

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Navigate to={APP_ROUTES.adminLogin} replace />} />
        <Route path={APP_ROUTES.adminLogin} element={<LoginPage />} />
        <Route path={APP_ROUTES.adminSignup} element={<SignupPage />} />
      </Routes>
    </BrowserRouter>
  );
}