import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import AdminPageHeader from "../../components/admin/AdminPageHeader";
import AdminStatCard from "../../components/admin/AdminStatCard";
import { LIVE_REFRESH_INTERVAL_MS } from "../../constants/live";
import { APP_ROUTES, getAdminBinDetailRoute } from "../../constants/routes";
import {
  fetchAdminBinsOverview,
  fetchAdminSummary,
  fetchDevices
} from "../../services/adminService";
import {
  formatBinStatusLabel,
  formatMetric,
  formatTimestamp
} from "../../utils/adminFormatters";
import "../../styles/AdminPages.css";

export default function AdminDashboardPage() {
  const [summary, setSummary] = useState(null);
  const [binsOverview, setBinsOverview] = useState([]);
  const [devicesCount, setDevicesCount] = useState(0);
  const [errorMessage, setErrorMessage] = useState("");
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    let isMounted = true;

    async function loadDashboardData() {
      try {
        setIsLoading(true);
        setErrorMessage("");

        const [summaryData, binsData, devicesData] = await Promise.all([
          fetchAdminSummary(),
          fetchAdminBinsOverview(),
          fetchDevices()
        ]);

        if (!isMounted) {
          return;
        }

        setSummary(summaryData.summary);
        setBinsOverview(binsData.bins);
        setDevicesCount(devicesData.count);
      } catch (error) {
        if (!isMounted) {
          return;
        }

        setErrorMessage(error.message || "Failed to load dashboard data.");
      } finally {
        if (isMounted) {
          setIsLoading(false);
        }
      }
    }

    loadDashboardData();
    const intervalId = window.setInterval(loadDashboardData, LIVE_REFRESH_INTERVAL_MS);

    return () => {
      isMounted = false;
      window.clearInterval(intervalId);
    };
  }, []);

  const fullBinsCount = binsOverview.filter((bin) => bin.is_full).length;
  const binsWithLiveReadings = binsOverview.filter((bin) => bin.updated_at).length;
  const latestDeviceCheckIn = binsOverview
    .map((bin) => bin.last_seen_at)
    .filter(Boolean)
    .sort((a, b) => new Date(b) - new Date(a))[0];

  return (
    <section className="admin-page-section">
      <AdminPageHeader
        eyebrow="Admin Overview"
        title="Operations Dashboard"
        description="System summary for live bin status, recent device activity, and current operational priorities."
      />

      {errorMessage ? <p className="admin-page-error">{errorMessage}</p> : null}

      <section className="admin-stats-grid">
        <AdminStatCard label="Total Users" value={summary?.total_users ?? "--"} />
        <AdminStatCard label="Total Bins" value={summary?.total_bins ?? "--"} />
        <AdminStatCard label="Active Bins" value={summary?.active_bins ?? "--"} />
        <AdminStatCard label="Registered Devices" value={devicesCount} />
      </section>

      <section className="admin-two-column">
        <article className="admin-section-card">
          <div className="admin-section-header">
            <div>
              <h2>Attention Summary</h2>
              <p>
                Quick operational signals pulled from the latest bin and device
                data.
              </p>
            </div>
          </div>

          <dl className="admin-definition-list">
            <div>
              <dt>Bins needing collection</dt>
              <dd>{fullBinsCount}</dd>
            </div>
            <div>
              <dt>Bins with live data</dt>
              <dd>{binsWithLiveReadings}</dd>
            </div>
            <div>
              <dt>Latest device check-in</dt>
              <dd>{formatTimestamp(latestDeviceCheckIn)}</dd>
            </div>
            <div>
              <dt>Refresh interval</dt>
              <dd>30 seconds</dd>
            </div>
          </dl>
        </article>

        <article className="admin-section-card">
          <div className="admin-section-header">
            <div>
              <h2>Quick access</h2>
              <p>Direct links to the main monitoring pages.</p>
            </div>
          </div>

          <div className="admin-action-list">
            <div className="admin-action-item">
              <strong>Live Bin Monitor</strong>
              <p>
                <Link to={APP_ROUTES.adminBins}>
                  Bin status, telemetry, and recent readings
                </Link>
              </p>
            </div>
            <div className="admin-action-item">
              <strong>Device Registry</strong>
              <p>
                <Link to={APP_ROUTES.adminDevices}>
                  Device status and last heartbeat records
                </Link>
              </p>
            </div>
          </div>
        </article>
      </section>

      <section className="admin-section-card">
        <div className="admin-section-header">
          <div>
            <h2>Bin Overview</h2>
            <p>
              Current operational snapshot for each registered bin.
            </p>
          </div>
          <span className="admin-refresh-badge">
            {isLoading ? "Loading..." : `${binsOverview.length} bins`}
          </span>
        </div>

        <div className="admin-table-wrapper">
          <table className="admin-table">
            <thead>
              <tr>
                <th>Bin</th>
                <th>Fill Level</th>
                <th>Weight</th>
                <th>Status</th>
                <th>Devices</th>
                <th>Last Seen</th>
              </tr>
            </thead>
            <tbody>
              {binsOverview.length === 0 && !isLoading ? (
                <tr>
                  <td colSpan="6" className="admin-empty-state">
                    No bins available.
                  </td>
                </tr>
              ) : null}

              {binsOverview.map((bin) => (
                <tr key={bin.bin_id}>
                  <td>
                    <div className="admin-bin-cell">
                      <strong>
                        <Link to={getAdminBinDetailRoute(bin.bin_id)}>
                          {bin.public_code}
                        </Link>
                      </strong>
                      <span>Bin #{bin.bin_id}</span>
                    </div>
                  </td>
                  <td>{formatMetric(bin.fill_pct, "%")}</td>
                  <td>{formatMetric(bin.weight_kg, " kg")}</td>
                  <td>
                    <span
                      className={`admin-state-pill ${
                        bin.is_full
                          ? "admin-state-pill--alert"
                          : "admin-state-pill--safe"
                      }`}
                    >
                      {formatBinStatusLabel(bin.is_full, bin.is_active)}
                    </span>
                  </td>
                  <td>{bin.device_count}</td>
                  <td>{formatTimestamp(bin.last_seen_at)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>
    </section>
  );
}
