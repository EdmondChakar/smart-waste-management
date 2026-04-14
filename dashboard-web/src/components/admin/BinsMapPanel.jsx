import { formatBinStatusLabel, formatGps, formatTimestamp } from "../../utils/adminFormatters";

function getMarkerPosition(bin, binsWithGps) {
  if (binsWithGps.length === 1) {
    return { left: "50%", top: "50%" };
  }

  const lats = binsWithGps.map((item) => item.lat);
  const lons = binsWithGps.map((item) => item.lon);
  const minLat = Math.min(...lats);
  const maxLat = Math.max(...lats);
  const minLon = Math.min(...lons);
  const maxLon = Math.max(...lons);

  const lonRange = maxLon - minLon || 1;
  const latRange = maxLat - minLat || 1;
  const leftPercent = 12 + ((bin.lon - minLon) / lonRange) * 76;
  const topPercent = 12 + ((maxLat - bin.lat) / latRange) * 76;

  return {
    left: `${leftPercent}%`,
    top: `${topPercent}%`
  };
}

export default function BinsMapPanel({
  bins,
  selectedBinId,
  onSelectBin
}) {
  const binsWithGps = bins.filter(
    (bin) => bin.lat !== null && bin.lat !== undefined && bin.lon !== null && bin.lon !== undefined
  );
  const selectedBin =
    bins.find((bin) => bin.bin_id === selectedBinId) ??
    binsWithGps[0] ??
    bins[0] ??
    null;

  return (
    <section className="admin-map-layout">
      <article className="admin-section-card">
        <div className="admin-section-header">
          <div>
            <h2>Bin map</h2>
            <p>Latest recorded coordinates for bins with GPS data.</p>
          </div>
        </div>

        {binsWithGps.length > 0 ? (
          <div className="admin-map-surface">
            <div className="admin-map-grid" />
            {binsWithGps.map((bin) => {
              const markerPosition = getMarkerPosition(bin, binsWithGps);

              return (
                <button
                  key={bin.bin_id}
                  type="button"
                  className={`admin-map-marker ${
                    selectedBin?.bin_id === bin.bin_id
                      ? "admin-map-marker--selected"
                      : ""
                  } ${
                    bin.is_full
                      ? "admin-map-marker--alert"
                      : "admin-map-marker--safe"
                  }`}
                  style={markerPosition}
                  onClick={() => onSelectBin(bin.bin_id)}
                  title={`${bin.public_code} - ${formatGps(bin.lat, bin.lon)}`}
                >
                  <span>{bin.public_code}</span>
                </button>
              );
            })}
          </div>
        ) : (
          <div className="admin-map-surface admin-map-surface--empty">
            <div className="admin-location-empty">
              No GPS coordinates are currently available.
            </div>
          </div>
        )}
      </article>

      <article className="admin-section-card">
        <div className="admin-section-header">
          <div>
            <h2>Selected bin</h2>
            <p>Operational snapshot for the highlighted bin.</p>
          </div>
        </div>

        {selectedBin ? (
          <div className="admin-map-sidebar">
            <div className="admin-action-item">
              <strong>{selectedBin.public_code}</strong>
              <p>{formatBinStatusLabel(selectedBin.is_full, selectedBin.is_active)}</p>
            </div>
            <div className="admin-action-item">
              <strong>Coordinates</strong>
              <p>{formatGps(selectedBin.lat, selectedBin.lon)}</p>
            </div>
            <div className="admin-action-item">
              <strong>Last update</strong>
              <p>{formatTimestamp(selectedBin.updated_at)}</p>
            </div>
            <div className="admin-action-item">
              <strong>Fill and weight</strong>
              <p>
                {selectedBin.fill_pct ?? "No data"}% | {selectedBin.weight_kg ?? "No data"} kg
              </p>
            </div>
          </div>
        ) : (
          <div className="admin-location-empty">
            No bins available.
          </div>
        )}
      </article>
    </section>
  );
}
