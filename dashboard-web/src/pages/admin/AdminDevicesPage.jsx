import { useEffect, useState } from "react";
import AdminPageHeader from "../../components/admin/AdminPageHeader";
import AdminStatCard from "../../components/admin/AdminStatCard";
import { LIVE_REFRESH_INTERVAL_MS } from "../../constants/live";
import { fetchDevices } from "../../services/adminService";
import { formatTimestamp } from "../../utils/adminFormatters";
import "../../styles/AdminPages.css";

export default function AdminDevicesPage() {
  const [devices, setDevices] = useState([]);
  const [errorMessage, setErrorMessage] = useState("");
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    let isMounted = true;

    async function loadDevices() {
      try {
        if (isMounted) {
          setErrorMessage("");
          setIsLoading(true);
        }

        const data = await fetchDevices();

        if (!isMounted) {
          return;
        }

        setDevices(data.devices);
      } catch (error) {
        if (isMounted) {
          setErrorMessage(error.message || "Failed to load devices.");
        }
      } finally {
        if (isMounted) {
          setIsLoading(false);
        }
      }
    }

    loadDevices();
    const intervalId = window.setInterval(loadDevices, LIVE_REFRESH_INTERVAL_MS);

    return () => {
      isMounted = false;
      window.clearInterval(intervalId);
    };
  }, []);

  const activeDevices = devices.filter((device) => device.is_active).length;
  const devicesSeenRecently = devices.filter((device) => {
    if (!device.last_seen_at) {
      return false;
    }

    const diffMs = Date.now() - new Date(device.last_seen_at).getTime();
    return diffMs <= 2 * 60 * 1000;
  }).length;

  return (
    <section className="admin-page-section">
      <AdminPageHeader
        eyebrow="Device Operations"
        title="Registered Devices"
        description="Device registry, activation state, and recent heartbeat activity."
      />

      {errorMessage ? <p className="admin-page-error">{errorMessage}</p> : null}

      <section className="admin-stats-grid">
        <AdminStatCard
          label="Registered devices"
          value={devices.length}
          helper="Total devices currently stored in the backend."
        />
        <AdminStatCard
          label="Active devices"
          value={activeDevices}
          helper="Devices marked as active and allowed to ingest readings."
        />
        <AdminStatCard
          label="Recent heartbeats"
          value={devicesSeenRecently}
          helper="Devices seen within the last 2 minutes."
        />
        <AdminStatCard
          label="Refresh interval"
          value="30s"
          helper="Dashboard polling cycle for this page."
        />
      </section>

      <section className="admin-section-card">
        <div className="admin-section-header">
          <div>
            <h2>Device Registry</h2>
            <p>Current device identity and latest heartbeat information.</p>
          </div>
          <span className="admin-refresh-badge">
            {isLoading ? "Refreshing..." : `${devices.length} device(s)`}
          </span>
        </div>

        <div className="admin-table-wrapper">
          <table className="admin-table">
            <thead>
              <tr>
                <th>Device ID</th>
                <th>Device UID</th>
                <th>Bin ID</th>
                <th>Active</th>
                <th>Last Seen</th>
              </tr>
            </thead>
            <tbody>
              {devices.length === 0 && !isLoading ? (
                <tr>
                  <td colSpan="5" className="admin-empty-state">
                    No devices registered.
                  </td>
                </tr>
              ) : null}

              {devices.map((device) => (
                <tr key={device.device_id}>
                  <td>{device.device_id}</td>
                  <td>{device.device_uid}</td>
                  <td>{device.bin_id}</td>
                  <td>
                    <span
                      className={`admin-state-pill ${
                        device.is_active
                          ? "admin-state-pill--safe"
                          : "admin-state-pill--alert"
                      }`}
                    >
                      {device.is_active ? "Active" : "Inactive"}
                    </span>
                  </td>
                  <td>{formatTimestamp(device.last_seen_at)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>
    </section>
  );
}
