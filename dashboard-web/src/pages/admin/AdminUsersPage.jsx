import { useEffect, useMemo, useState } from "react";
import AdminPageHeader from "../../components/admin/AdminPageHeader";
import AdminStatCard from "../../components/admin/AdminStatCard";
import { LIVE_REFRESH_INTERVAL_MS } from "../../constants/live";
import {
  fetchRoles,
  fetchUsers,
  fetchUserStatuses
} from "../../services/adminService";
import { formatTimestamp } from "../../utils/adminFormatters";
import "../../styles/AdminPages.css";

function toLookupMap(items, keyField, valueField) {
  return new Map(items.map((item) => [item[keyField], item[valueField]]));
}

export default function AdminUsersPage() {
  const [users, setUsers] = useState([]);
  const [roles, setRoles] = useState([]);
  const [statuses, setStatuses] = useState([]);
  const [searchTerm, setSearchTerm] = useState("");
  const [roleFilter, setRoleFilter] = useState("all");
  const [statusFilter, setStatusFilter] = useState("all");
  const [isLoading, setIsLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState("");

  useEffect(() => {
    let isMounted = true;

    async function loadUsersPageData() {
      try {
        if (isMounted) {
          setIsLoading(true);
          setErrorMessage("");
        }

        const [usersData, rolesData, statusesData] = await Promise.all([
          fetchUsers(),
          fetchRoles(),
          fetchUserStatuses()
        ]);

        if (!isMounted) {
          return;
        }

        setUsers(usersData.users);
        setRoles(rolesData.roles);
        setStatuses(statusesData.user_statuses);
      } catch (error) {
        if (isMounted) {
          setErrorMessage(error.message || "Failed to load users.");
        }
      } finally {
        if (isMounted) {
          setIsLoading(false);
        }
      }
    }

    loadUsersPageData();
    const intervalId = window.setInterval(loadUsersPageData, LIVE_REFRESH_INTERVAL_MS);

    return () => {
      isMounted = false;
      window.clearInterval(intervalId);
    };
  }, []);

  const roleMap = useMemo(() => toLookupMap(roles, "role_id", "role_name"), [roles]);
  const statusMap = useMemo(
    () => toLookupMap(statuses, "status_id", "status_name"),
    [statuses]
  );

  const filteredUsers = useMemo(() => {
    const normalizedSearch = searchTerm.trim().toLowerCase();

    return users.filter((user) => {
      const matchesSearch =
        !normalizedSearch || user.email.toLowerCase().includes(normalizedSearch);
      const matchesRole =
        roleFilter === "all" || String(user.role_id) === roleFilter;
      const matchesStatus =
        statusFilter === "all" || String(user.status_id) === statusFilter;

      return matchesSearch && matchesRole && matchesStatus;
    });
  }, [users, searchTerm, roleFilter, statusFilter]);

  const activeUsersCount = users.filter((user) => statusMap.get(user.status_id) === "ACTIVE").length;
  const adminUsersCount = users.filter((user) => roleMap.get(user.role_id) === "ADMIN").length;
  const standardUsersCount = users.filter((user) => roleMap.get(user.role_id) === "USER").length;

  return (
    <section className="admin-page-section">
      <AdminPageHeader
        eyebrow="User Management"
        title="Users"
        description="Registered accounts, role assignments, and account status from the current backend dataset."
      />

      {errorMessage ? <p className="admin-page-error">{errorMessage}</p> : null}

      <section className="admin-stats-grid">
        <AdminStatCard
          label="Total users"
          value={users.length}
          helper="All accounts currently stored in the system."
        />
        <AdminStatCard
          label="Active users"
          value={activeUsersCount}
          helper="Accounts marked active in user status records."
        />
        <AdminStatCard
          label="Admin accounts"
          value={adminUsersCount}
          helper="Accounts assigned the admin role."
        />
        <AdminStatCard
          label="Citizen accounts"
          value={standardUsersCount}
          helper="Accounts assigned the standard user role."
        />
      </section>

      <section className="admin-section-card">
        <div className="admin-section-header">
          <div>
            <h2>User directory</h2>
            <p>Search by email and filter the list by role or account status.</p>
          </div>
        </div>

        <div className="admin-filter-row">
          <input
            className="admin-search-input"
            type="search"
            placeholder="Search by email"
            value={searchTerm}
            onChange={(event) => setSearchTerm(event.target.value)}
          />

          <select
            className="admin-select-input"
            value={roleFilter}
            onChange={(event) => setRoleFilter(event.target.value)}
          >
            <option value="all">All roles</option>
            {roles.map((role) => (
              <option key={role.role_id} value={String(role.role_id)}>
                {role.role_name}
              </option>
            ))}
          </select>

          <select
            className="admin-select-input"
            value={statusFilter}
            onChange={(event) => setStatusFilter(event.target.value)}
          >
            <option value="all">All statuses</option>
            {statuses.map((status) => (
              <option key={status.status_id} value={String(status.status_id)}>
                {status.status_name}
              </option>
            ))}
          </select>
        </div>

        <div className="admin-table-wrapper">
          <table className="admin-table">
            <thead>
              <tr>
                <th>User ID</th>
                <th>Email</th>
                <th>Role</th>
                <th>Status</th>
                <th>Created</th>
              </tr>
            </thead>
            <tbody>
              {filteredUsers.length === 0 && !isLoading ? (
                <tr>
                  <td colSpan="5" className="admin-empty-state">
                    No users match the current filters.
                  </td>
                </tr>
              ) : null}

              {filteredUsers.map((user) => {
                const roleName = roleMap.get(user.role_id) || `Role ${user.role_id}`;
                const statusName =
                  statusMap.get(user.status_id) || `Status ${user.status_id}`;

                return (
                  <tr key={user.user_id}>
                    <td>{user.user_id}</td>
                    <td>{user.email}</td>
                    <td>
                      <span className="admin-table-badge">{roleName}</span>
                    </td>
                    <td>
                      <span
                        className={`admin-state-pill ${
                          statusName === "ACTIVE"
                            ? "admin-state-pill--safe"
                            : "admin-state-pill--muted"
                        }`}
                      >
                        {statusName}
                      </span>
                    </td>
                    <td>{formatTimestamp(user.created_at)}</td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>

        <p className="admin-data-note">
          Contribution history, points, and reward activity are not available in the current backend user response yet.
        </p>
      </section>
    </section>
  );
}
