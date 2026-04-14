import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import BinsMapPanel from "../../components/admin/BinsMapPanel";
import AdminPageHeader from "../../components/admin/AdminPageHeader";
import AdminStatCard from "../../components/admin/AdminStatCard";
import { LIVE_REFRESH_INTERVAL_MS } from "../../constants/live";
import { getAdminBinDetailRoute } from "../../constants/routes";
import { fetchAdminBinsOverview } from "../../services/adminService";
import {
  formatBinStatusLabel,
  formatGps,
  formatTimestamp
} from "../../utils/adminFormatters";
import "../../styles/AdminPages.css";

export default function AdminMapPage() {
  const [binsOverview, setBinsOverview] = useState([]);
  const [selectedBinId, setSelectedBinId] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState("");

  useEffect(() => {
    let isMounted = true;

    async function loadBinsOverview() {
      try {
        if (isMounted) {
          setIsLoading(true);
          setErrorMessage("");
        }

        const overviewData = await fetchAdminBinsOverview();

        if (isMounted) {
          setBinsOverview(overviewData.bins);
          setSelectedBinId((currentSelectedBinId) => {
            if (
              currentSelectedBinId &&
              overviewData.bins.some((bin) => bin.bin_id === currentSelectedBinId)
            ) {
              return currentSelectedBinId;
            }

            const firstGpsBin = overviewData.bins.find(
              (bin) => bin.lat !== null && bin.lat !== undefined && bin.lon !== null && bin.lon !== undefined
            );

            return firstGpsBin?.bin_id ?? overviewData.bins[0]?.bin_id ?? null;
          });
        }
      } catch (error) {
        if (isMounted) {
          setErrorMessage(error.message || "Failed to load map data.");
        }
      } finally {
        if (isMounted) {
          setIsLoading(false);
        }
      }
    }

    loadBinsOverview();
    const intervalId = window.setInterval(loadBinsOverview, LIVE_REFRESH_INTERVAL_MS);

    return () => {
      isMounted = false;
      window.clearInterval(intervalId);
    };
  }, []);

  const binsWithGps = useMemo(
    () =>
      binsOverview.filter(
        (bin) => bin.lat !== null && bin.lat !== undefined && bin.lon !== null && bin.lon !== undefined
      ),
    [binsOverview]
  );
  const fullBinsCount = binsOverview.filter((bin) => bin.is_full).length;
  const selectedBin =
    binsOverview.find((bin) => bin.bin_id === selectedBinId) ??
    binsWithGps[0] ??
    binsOverview[0] ??
    null;

  return (
    <section className="admin-page-section">
      <AdminPageHeader
        eyebrow="Monitoring Map"
        title="Bin location view"
        description="Map-based view of the latest recorded bin locations and operational status."
      />

      {errorMessage ? <p className="admin-page-error">{errorMessage}</p> : null}

      <section className="admin-stats-grid">
        <AdminStatCard
          label="Bins on map"
          value={binsWithGps.length}
          helper="Bins that currently have valid GPS coordinates."
        />
        <AdminStatCard
          label="GPS pending"
          value={binsOverview.length - binsWithGps.length}
          helper="Bins still waiting for their first location update."
        />
        <AdminStatCard
          label="Full bins"
          value={fullBinsCount}
          helper="Bins currently marked full by the backend."
        />
        <AdminStatCard
          label="Refresh interval"
          value="30s"
          helper="Map polling interval."
        />
      </section>

      <BinsMapPanel
        bins={binsOverview}
        selectedBinId={selectedBinId}
        onSelectBin={setSelectedBinId}
      />

      <section className="admin-section-card">
        <div className="admin-section-header">
          <div>
            <h2>GPS status list</h2>
            <p>Quick verification list for the coordinates currently available in the backend.</p>
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
                <th>Status</th>
                <th>Coordinates</th>
                <th>Last Update</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              {binsOverview.length === 0 && !isLoading ? (
                <tr>
                  <td colSpan="5" className="admin-empty-state">
                    No bins available.
                  </td>
                </tr>
              ) : null}

              {binsOverview.map((bin) => (
                <tr
                  key={bin.bin_id}
                  className={
                    selectedBin?.bin_id === bin.bin_id ? "admin-table-row--selected" : ""
                  }
                  onClick={() => setSelectedBinId(bin.bin_id)}
                >
                  <td>
                    <div className="admin-bin-cell">
                      <strong>{bin.public_code}</strong>
                      <span>Bin #{bin.bin_id}</span>
                    </div>
                  </td>
                  <td>
                    <span
                      className={`admin-state-pill ${
                        bin.is_active
                          ? bin.is_full
                            ? "admin-state-pill--alert"
                            : "admin-state-pill--safe"
                          : "admin-state-pill--muted"
                      }`}
                    >
                      {formatBinStatusLabel(bin.is_full, bin.is_active)}
                    </span>
                  </td>
                  <td>{formatGps(bin.lat, bin.lon)}</td>
                  <td>{formatTimestamp(bin.updated_at)}</td>
                  <td>
                    <Link
                      className="admin-link-button"
                      to={getAdminBinDetailRoute(bin.bin_id)}
                      onClick={(event) => event.stopPropagation()}
                    >
                      Open details
                    </Link>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>
    </section>
  );
}
