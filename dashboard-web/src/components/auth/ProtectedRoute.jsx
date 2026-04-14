import { Navigate, Outlet, useLocation } from "react-router-dom";
import { APP_ROUTES } from "../../constants/routes";
import { getStoredUser, isAuthenticated } from "../../services/sessionService";

export default function ProtectedRoute() {
  const location = useLocation();
  const currentUser = getStoredUser();

  if (!isAuthenticated() || !currentUser) {
    return (
      <Navigate
        to={APP_ROUTES.adminLogin}
        replace
        state={{ from: location.pathname }}
      />
    );
  }

  if (currentUser.role_id !== 2) {
    return <Navigate to={APP_ROUTES.adminLogin} replace />;
  }

  return <Outlet />;
}
