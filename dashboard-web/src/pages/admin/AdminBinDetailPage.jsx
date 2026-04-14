import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import AdminPageHeader from "../../components/admin/AdminPageHeader";
import AdminStatCard from "../../components/admin/AdminStatCard";
import { LIVE_REFRESH_INTERVAL_MS } from "../../constants/live";
import { APP_ROUTES } from "../../constants/routes";
import {
  fetchAdminBinDetail,
  fetchRecentBinReadings
} from "../../services/adminService";
import {
  formatBinStatusLabel,
  formatGps,
  formatMetric,
  formatTimestamp
} from "../../utils/adminFormatters";
import "../../styles/AdminPages.css";

export default function AdminBinDetailPage() {
  const { binId } = useParams();
  const [binDetail, setBinDetail] = useState(null);
  const [readings, setReadings] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState("");

  useEffect(() => {
    let isMounted = true;

    async function loadBinData() {
      try {
        if (isMounted) {
          setIsLoading(true);
          setErrorMessage("");
        }

        const [detailData, readingsData] = await Promise.all([
          fetchAdminBinDetail(binId),
          fetchRecentBinReadings(binId, 12)
        ]);

        if (!isMounted) {
          return;
        }

        setBinDetail(detailData);
        setReadings(readingsData.readings);
      } catch (error) {
        if (isMounted) {
          setErrorMessage(error.message || "Failed to load bin details.");
        }
      } finally {
        if (isMounted) {
          setIsLoading(false);
        }
      }
    }

    loadBinData();
    const intervalId = window.setInterval(loadBinData, LIVE_REFRESH_INTERVAL_MS);

    return () => {
      isMounted = false;
      window.clearInterval(intervalId);
    };
  }, [binId]);

  return (
    <section className="admin-page-section">
      <AdminPageHeader
        eyebrow="Bin Details"
        title={binDetail ? `Bin ${binDetail.public_code}` : "Bin details"}
        description="Detailed operational profile for one smart bin, including current status, location, and recent telemetry."
        actions={
          <div className="admin-inline-actions">
            <Link className="admin-link-button" to={APP_ROUTES.adminBins}>
              Back to bins
            </Link>
          </div>
        }
      />

      {errorMessage ? <p className="admin-page-error">{errorMessage}</p> : null}

      {binDetail ? (
        <>
        <section className="admin-stats-grid">
            <AdminStatCard
              label="Current status"
              value={formatBinStatusLabel(binDetail.is_full, binDetail.is_active)}
              helper={
                binDetail.is_full
                  ? "This bin is currently flagged as full and should be prioritized for collection."
                  : "This bin is currently below the configured full threshold."
              }
            />
            <AdminStatCard
              label="Current fill"
              value={formatMetric(binDetail.fill_pct, "%")}
              helper="Latest stored ultrasonic reading."
            />
            <AdminStatCard
              label="Current weight"
              value={formatMetric(binDetail.weight_kg, " kg")}
              helper="Latest stored load-cell reading."
            />
            <AdminStatCard
              label="Current location"
              value={formatGps(binDetail.lat, binDetail.lon)}
              helper="Shows the last known GPS coordinates."
            />
            <AdminStatCard
              label="Last update"
              value={formatTimestamp(binDetail.updated_at)}
              helper={`${binDetail.device_count} linked device(s)`}
            />
          </section>

          <section className="admin-two-column">
            <article className="admin-section-card">
              <div className="admin-section-header">
                <div>
                  <h2>Current status</h2>
                  <p>Latest information pulled from the backend status tables.</p>
                </div>
                <span
                  className={`admin-state-pill ${
                    binDetail.is_full
                      ? "admin-state-pill--alert"
                      : "admin-state-pill--safe"
                  }`}
                >
                  {formatBinStatusLabel(binDetail.is_full, binDetail.is_active)}
                </span>
              </div>

              {binDetail.is_full ? (
                <div className="admin-status-banner admin-status-banner--alert">
                  This bin is currently marked as full by the backend and should be prioritized for collection.
                </div>
              ) : null}

              <dl className="admin-definition-list">
                <div>
                  <dt>Bin code</dt>
                  <dd>{binDetail.public_code}</dd>
                </div>
                <div>
                  <dt>Bin active</dt>
                  <dd>{binDetail.is_active ? "Yes" : "No"}</dd>
                </div>
                <div>
                  <dt>Last reading ID</dt>
                  <dd>{binDetail.last_reading_id ?? "No reading data"}</dd>
                </div>
                <div>
                  <dt>Last device check-in</dt>
                  <dd>{formatTimestamp(binDetail.last_seen_at)}</dd>
                </div>
              </dl>
            </article>

            <article className="admin-section-card">
              <div className="admin-section-header">
                <div>
                  <h2>Operational notes</h2>
                  <p>Quick guidance based on the latest telemetry.</p>
                </div>
              </div>

              <div className="admin-action-list">
                <div className="admin-action-item">
                  <strong>Location status</strong>
                  <p>{formatGps(binDetail.lat, binDetail.lon)}</p>
                </div>
                <div className="admin-action-item">
                  <strong>Collection priority</strong>
                  <p>
                    {binDetail.is_full
                      ? "This bin should be prioritized for collection."
                      : "This bin does not currently require immediate collection."}
                  </p>
                </div>
                <div className="admin-action-item">
                  <strong>Readings refresh</strong>
                  <p>This page refreshes every 30 seconds.</p>
                </div>
              </div>
            </article>
          </section>

          <section className="admin-section-card">
            <div className="admin-section-header">
              <div>
                <h2>Recent readings</h2>
                <p>Latest telemetry records stored for this bin.</p>
              </div>
              <span className="admin-refresh-badge">
                {isLoading ? "Refreshing..." : `${readings.length} rows`}
              </span>
            </div>

            <div className="admin-table-wrapper">
              <table className="admin-table">
                <thead>
                  <tr>
                    <th>Captured At</th>
                    <th>Fill %</th>
                    <th>Weight</th>
                    <th>GPS</th>
                    <th>Device ID</th>
                  </tr>
                </thead>
                <tbody>
                  {readings.length === 0 && !isLoading ? (
                    <tr>
                      <td colSpan="5" className="admin-empty-state">
                        No reading data is available for this bin.
                      </td>
                    </tr>
                  ) : null}

                  {readings.map((reading) => (
                    <tr key={reading.reading_id}>
                      <td>{formatTimestamp(reading.captured_at)}</td>
                      <td>{formatMetric(reading.fill_pct, "%")}</td>
                      <td>{formatMetric(reading.weight_kg, " kg")}</td>
                      <td>{formatGps(reading.gps_lat, reading.gps_lon)}</td>
                      <td>{reading.device_id}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </section>
        </>
      ) : (
        <section className="admin-section-card">
          <p className="admin-empty-note">
            {isLoading ? "Loading bin details..." : "Bin details are unavailable."}
          </p>
        </section>
      )}
    </section>
  );
}
