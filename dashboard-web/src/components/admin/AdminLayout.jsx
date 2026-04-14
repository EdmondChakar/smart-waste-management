import { NavLink, Outlet, useNavigate } from "react-router-dom";
import { APP_ROUTES } from "../../constants/routes";
import { clearSession, getStoredUser } from "../../services/sessionService";
import "../../styles/AdminLayout.css";

function getNavClassName({ isActive }) {
  return isActive ? "admin-nav-link admin-nav-link--active" : "admin-nav-link";
}

export default function AdminLayout() {
  const navigate = useNavigate();
  const currentUser = getStoredUser();

  const handleLogout = () => {
    clearSession();
    navigate(APP_ROUTES.adminLogin, { replace: true });
  };

  return (
    <div className="admin-shell">
      <aside className="admin-sidebar">
        <div>
          <p className="admin-sidebar-eyebrow">SWM Admin</p>
          <h1 className="admin-sidebar-title">Operations</h1>
          <p className="admin-sidebar-copy">
            Bin monitoring, device status, and operational controls.
          </p>
        </div>

        <nav className="admin-nav">
          <NavLink to={APP_ROUTES.adminDashboard} className={getNavClassName}>
            Dashboard
          </NavLink>
          <NavLink to={APP_ROUTES.adminBins} className={getNavClassName}>
            Bin Monitor
          </NavLink>
          <NavLink to={APP_ROUTES.adminMap} className={getNavClassName}>
            Map
          </NavLink>
          <NavLink to={APP_ROUTES.adminUsers} className={getNavClassName}>
            Users
          </NavLink>
          <NavLink to={APP_ROUTES.adminDevices} className={getNavClassName}>
            Devices
          </NavLink>
        </nav>

        <div className="admin-sidebar-footer">
          <div className="admin-sidebar-user">
            <span>Signed in as</span>
            <strong>{currentUser?.email || "Admin"}</strong>
          </div>

          <button className="admin-sidebar-logout" onClick={handleLogout}>
            Logout
          </button>
        </div>
      </aside>

      <div className="admin-main">
        <header className="admin-topbar">
          <div>
            <p className="admin-topbar-label">Monitoring</p>
            <h2>Live operational view</h2>
          </div>
        </header>

        <Outlet />
      </div>
    </div>
  );
}
