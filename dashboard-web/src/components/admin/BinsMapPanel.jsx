import { useEffect, useMemo, useRef } from "react";
import L from "leaflet";
import "leaflet/dist/leaflet.css";
import {
  formatBinStatusLabel,
  formatGps,
  formatTimestamp
} from "../../utils/adminFormatters";

function getSafeMapZoom(map, fallbackZoom = 15) {
  try {
    const zoom = map.getZoom();
    return Number.isFinite(zoom) ? Math.max(zoom, fallbackZoom) : fallbackZoom;
  } catch {
    return fallbackZoom;
  }
}

function escapeHtml(value) {
  return String(value)
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#39;");
}

function createBinIcon(bin, isSelected) {
  const stateClass = bin.is_full
    ? "admin-map-pin--alert"
    : "admin-map-pin--safe";
  const selectedClass = isSelected ? "admin-map-pin--selected" : "";

  return L.divIcon({
    className: "admin-map-pin-wrap",
    html: `
      <div class="admin-map-pin ${stateClass} ${selectedClass}">
        <span>${escapeHtml(bin.public_code)}</span>
      </div>
    `,
    iconSize: [96, 42],
    iconAnchor: [48, 42],
    tooltipAnchor: [0, -34]
  });
}

export default function BinsMapPanel({
  bins,
  selectedBinId,
  onSelectBin
}) {
  const mapContainerRef = useRef(null);
  const mapRef = useRef(null);
  const markersLayerRef = useRef(null);

  const binsWithGps = useMemo(
    () =>
      bins.filter(
        (bin) =>
          bin.lat !== null &&
          bin.lat !== undefined &&
          bin.lon !== null &&
          bin.lon !== undefined
      ),
    [bins]
  );

  const selectedBin =
    bins.find((bin) => bin.bin_id === selectedBinId) ??
    binsWithGps[0] ??
    bins[0] ??
    null;

  useEffect(() => {
    if (binsWithGps.length === 0 && mapRef.current) {
      mapRef.current.remove();
      mapRef.current = null;
      markersLayerRef.current = null;
    }
  }, [binsWithGps.length]);

  useEffect(() => {
    if (!mapContainerRef.current || binsWithGps.length === 0 || mapRef.current) {
      return undefined;
    }

    const map = L.map(mapContainerRef.current, {
      zoomControl: true,
      attributionControl: true
    });

    L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
      maxZoom: 19,
      attribution: "&copy; OpenStreetMap contributors"
    }).addTo(map);

    markersLayerRef.current = L.layerGroup().addTo(map);
    mapRef.current = map;

    return () => {
      if (mapRef.current) {
        mapRef.current.remove();
        mapRef.current = null;
        markersLayerRef.current = null;
      }
    };
  }, [binsWithGps.length]);

  useEffect(() => {
    const map = mapRef.current;
    const markersLayer = markersLayerRef.current;

    if (!map || !markersLayer) {
      return;
    }

    markersLayer.clearLayers();

    const bounds = [];

    binsWithGps.forEach((bin) => {
      const marker = L.marker([bin.lat, bin.lon], {
        icon: createBinIcon(bin, selectedBin?.bin_id === bin.bin_id)
      }).addTo(markersLayer);

      marker.on("click", () => onSelectBin(bin.bin_id));
      marker.bindTooltip(
        `${bin.public_code} - ${formatGps(bin.lat, bin.lon)}`,
        {
          direction: "top",
          offset: [0, -34]
        }
      );

      bounds.push([bin.lat, bin.lon]);
    });

    const selectedGpsBin = binsWithGps.find(
      (bin) => bin.bin_id === selectedBin?.bin_id
    );

    if (selectedGpsBin) {
      map.setView(
        [selectedGpsBin.lat, selectedGpsBin.lon],
        getSafeMapZoom(map, 15)
      );
    } else if (bounds.length === 1) {
      map.setView(bounds[0], 15);
    } else if (bounds.length > 1) {
      map.fitBounds(bounds, {
        padding: [36, 36]
      });
    }
  }, [binsWithGps, onSelectBin, selectedBin]);

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
          <div className="admin-map-surface admin-map-surface--leaflet">
            <div ref={mapContainerRef} className="admin-leaflet-map" />
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
