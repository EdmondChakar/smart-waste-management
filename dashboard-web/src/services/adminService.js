import { buildApiUrl } from "./api";
import { getAccessToken } from "./sessionService";

async function authorizedFetch(path) {
  const token = getAccessToken();
  const response = await fetch(buildApiUrl(path), {
    headers: {
      Authorization: `Bearer ${token}`
    }
  });

  const data = await response.json();

  if (!response.ok) {
    throw new Error(data.detail || "Request failed.");
  }

  return data;
}

export function fetchAdminSummary() {
  return authorizedFetch("/admin/summary");
}

export function fetchAdminBinsOverview() {
  return authorizedFetch("/admin/bins/overview");
}

export function fetchDevices() {
  return authorizedFetch("/devices");
}

export function fetchUsers() {
  return authorizedFetch("/users");
}

export function fetchRoles() {
  return authorizedFetch("/roles");
}

export function fetchUserStatuses() {
  return authorizedFetch("/user-statuses");
}

export function fetchAdminBinDetail(binId) {
  return authorizedFetch(`/admin/bins/${binId}`);
}

export function fetchRecentBinReadings(binId, limit = 20) {
  return authorizedFetch(`/admin/bins/${binId}/readings?limit=${limit}`);
}
