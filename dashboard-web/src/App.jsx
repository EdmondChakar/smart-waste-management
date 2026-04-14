import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom";
import AdminLayout from "./components/admin/AdminLayout";
import ProtectedRoute from "./components/auth/ProtectedRoute";
import { APP_ROUTES } from "./constants/routes";
import AdminBinDetailPage from "./pages/admin/AdminBinDetailPage";
import AdminBinsPage from "./pages/admin/AdminBinsPage";
import AdminDashboardPage from "./pages/admin/AdminDashboardPage";
import AdminDevicesPage from "./pages/admin/AdminDevicesPage";
import AdminMapPage from "./pages/admin/AdminMapPage";
import AdminUsersPage from "./pages/admin/AdminUsersPage";
import LoginPage from "./pages/auth/LoginPage";
import SignupPage from "./pages/auth/SignupPage";

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Navigate to={APP_ROUTES.adminLogin} replace />} />
        <Route path={APP_ROUTES.adminLogin} element={<LoginPage />} />
        <Route path={APP_ROUTES.adminSignup} element={<SignupPage />} />
        <Route element={<ProtectedRoute />}>
          <Route element={<AdminLayout />}>
            <Route
              path={APP_ROUTES.adminDashboard}
              element={<AdminDashboardPage />}
            />
            <Route path={APP_ROUTES.adminBins} element={<AdminBinsPage />} />
            <Route
              path={APP_ROUTES.adminBinDetailPattern}
              element={<AdminBinDetailPage />}
            />
            <Route path={APP_ROUTES.adminMap} element={<AdminMapPage />} />
            <Route path={APP_ROUTES.adminUsers} element={<AdminUsersPage />} />
            <Route path={APP_ROUTES.adminDevices} element={<AdminDevicesPage />} />
          </Route>
        </Route>
      </Routes>
    </BrowserRouter>
  );
}
