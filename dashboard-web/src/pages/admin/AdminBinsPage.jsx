import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import BinLocationPreview from "../../components/admin/BinLocationPreview";
import AdminPageHeader from "../../components/admin/AdminPageHeader";
import AdminStatCard from "../../components/admin/AdminStatCard";
import { LIVE_REFRESH_INTERVAL_MS } from "../../constants/live";
import { getAdminBinDetailRoute } from "../../constants/routes";
import { fetchAdminBinsOverview } from "../../services/adminService";
import {
  formatBinStatusLabel,
  formatGps,
  formatMetric,
  formatTimestamp
} from "../../utils/adminFormatters";
import "../../styles/AdminPages.css";

export default function AdminBinsPage() {
  const [binsOverview, setBinsOverview] = useState([]);
  const [selectedBinId, setSelectedBinId] = useState(null);
  const [searchTerm, setSearchTerm] = useState("");
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

            return overviewData.bins[0]?.bin_id ?? null;
          });
        }
      } catch (error) {
        if (isMounted) {
          setErrorMessage(error.message || "Failed to load bins.");
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

  const filteredBins = useMemo(() => {
    const normalizedSearch = searchTerm.trim().toLowerCase();

    if (!normalizedSearch) {
      return binsOverview;
    }

    return binsOverview.filter((bin) =>
      bin.public_code.toLowerCase().includes(normalizedSearch)
    );
  }, [binsOverview, searchTerm]);

  const fullBinsCount = binsOverview.filter((bin) => bin.is_full).length;
  const binsWithoutLocation = binsOverview.filter(
    (bin) => bin.updated_at && (bin.lat === null || bin.lon === null)
  ).length;
  const binsWithGps = binsOverview.filter(
    (bin) => bin.lat !== null && bin.lat !== undefined && bin.lon !== null && bin.lon !== undefined
  ).length;
  const binsWithRecentTelemetry = binsOverview.filter((bin) => bin.updated_at).length;
  const selectedBin =
    filteredBins.find((bin) => bin.bin_id === selectedBinId) ??
    binsOverview.find((bin) => bin.bin_id === selectedBinId) ??
    filteredBins[0] ??
    binsOverview[0] ??
    null;

  return (
    <section className="admin-page-section">
      <AdminPageHeader
        eyebrow="Bin Operations"
        title="Live Bin Monitor"
        description="Monitor the latest status of each bin, search by code, and open a detailed telemetry view for any specific bin."
      />

      {errorMessage ? <p className="admin-page-error">{errorMessage}</p> : null}

      <section className="admin-stats-grid">
        <AdminStatCard
          label="Tracked bins"
          value={binsOverview.length}
          helper="Bins currently visible in the monitoring table."
        />
        <AdminStatCard
          label="Full bins"
          value={fullBinsCount}
          helper="Bins that have reached the configured full threshold."
        />
        <AdminStatCard
          label="Live telemetry"
          value={binsWithRecentTelemetry}
          helper="Bins that already have recent backend status data."
        />
        <AdminStatCard
          label="GPS available"
          value={binsWithGps}
          helper="Bins with a last known latitude and longitude."
        />
        <AdminStatCard
          label="GPS pending"
          value={binsWithoutLocation}
          helper="Bins still waiting for GPS coordinates."
        />
      </section>

      <BinLocationPreview bin={selectedBin} />

      <section className="admin-section-card">
        <div className="admin-section-header">
          <div>
            <h2>Bins table</h2>
            <p>Search, select, and open a dedicated detail view for any bin.</p>
          </div>
          <input
            className="admin-search-input"
            type="search"
            placeholder="Search by bin code"
            value={searchTerm}
            onChange={(event) => setSearchTerm(event.target.value)}
          />
        </div>

        <div className="admin-table-wrapper">
          <table className="admin-table">
            <thead>
              <tr>
                <th>Bin</th>
                <th>Status</th>
                <th>Fill Level</th>
                <th>Weight</th>
                <th>Location</th>
                <th>Last Update</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              {filteredBins.length === 0 && !isLoading ? (
                <tr>
                  <td colSpan="7" className="admin-empty-state">
                    No bins match the current search.
                  </td>
                </tr>
              ) : null}

              {filteredBins.map((bin) => (
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
                    {bin.is_active ? (
                      <span
                        className={`admin-state-pill ${
                          bin.is_full
                            ? "admin-state-pill--alert"
                            : "admin-state-pill--safe"
                        }`}
                      >
                        {formatBinStatusLabel(bin.is_full, bin.is_active)}
                      </span>
                    ) : (
                      <span className="admin-state-pill admin-state-pill--muted">
                        Inactive
                      </span>
                    )}
                  </td>
                  <td>{formatMetric(bin.fill_pct, "%")}</td>
                  <td>{formatMetric(bin.weight_kg, " kg")}</td>
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
