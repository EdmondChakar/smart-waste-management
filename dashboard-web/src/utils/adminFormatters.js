export function formatMetric(value, suffix = "") {
  if (value === null || value === undefined) {
    return "No data";
  }

  return `${value}${suffix}`;
}

export function formatTimestamp(value) {
  if (!value) {
    return "No reading data";
  }

  return new Date(value).toLocaleString();
}

export function formatGps(lat, lon) {
  if (lat === null || lat === undefined || lon === null || lon === undefined) {
    return "GPS unavailable";
  }

  return `${lat.toFixed(5)}, ${lon.toFixed(5)}`;
}

export function formatBinStatusLabel(isFull, isActive = true) {
  if (!isActive) {
    return "Inactive";
  }

  return isFull ? "Full" : "Normal";
}
