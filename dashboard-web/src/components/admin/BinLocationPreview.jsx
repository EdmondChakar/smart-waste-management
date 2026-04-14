import { Link } from "react-router-dom";
import { getAdminBinDetailRoute } from "../../constants/routes";
import {
  formatBinStatusLabel,
  formatGps,
  formatTimestamp
} from "../../utils/adminFormatters";

export default function BinLocationPreview({ bin }) {
  if (!bin) {
    return (
      <article className="admin-section-card">
        <div className="admin-section-header">
          <div>
            <h2>Selected bin summary</h2>
            <p>Choose a bin from the monitoring table to review its latest status.</p>
          </div>
        </div>

        <div className="admin-location-empty">
          No bin selected.
        </div>
      </article>
    );
  }

  return (
    <article className="admin-section-card">
      <div className="admin-section-header">
        <div>
          <h2>Selected bin summary</h2>
          <p>Latest telemetry, location, and update status for the selected bin.</p>
        </div>
      </div>

      <div className="admin-compact-summary">
        <div className="admin-action-item">
          <strong>{bin.public_code}</strong>
          <p>
            Bin #{bin.bin_id}{" "}
            <Link to={getAdminBinDetailRoute(bin.bin_id)}>
              View profile
            </Link>
          </p>
        </div>
        <div className="admin-action-item">
          <strong>Status</strong>
          <p>
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
          </p>
        </div>
        <div className="admin-action-item">
          <strong>Fill level</strong>
          <p>{bin.fill_pct ?? "No data"}%</p>
        </div>
        <div className="admin-action-item">
          <strong>Weight</strong>
          <p>{bin.weight_kg ?? "No data"} kg</p>
        </div>
        <div className="admin-action-item">
          <strong>Current location</strong>
          <p>{formatGps(bin.lat, bin.lon)}</p>
        </div>
        <div className="admin-action-item">
          <strong>Last update</strong>
          <p>{formatTimestamp(bin.updated_at)}</p>
        </div>
      </div>
    </article>
  );
}
