import { buildApiUrl } from "./api";
import { getAccessToken } from "./sessionService";

async function authorizedRequest(path, options = {}) {
  const token = getAccessToken();
  const response = await fetch(buildApiUrl(path), {
    method: options.method || "GET",
    headers: {
      Authorization: `Bearer ${token}`,
      ...(options.body ? { "Content-Type": "application/json" } : {})
    },
    ...(options.body ? { body: JSON.stringify(options.body) } : {})
  });

  const data = await response.json();

  if (!response.ok) {
    throw new Error(data.detail || "Request failed.");
  }

  return data;
}

async function authorizedFetch(path) {
  return authorizedRequest(path);
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

export function fetchAdminRedemptions({ statusCode, limit = 50 } = {}) {
  const params = new URLSearchParams({ limit: String(limit) });

  if (statusCode && statusCode !== "all") {
    params.set("status_code", statusCode);
  }

  return authorizedFetch(`/admin/redemptions?${params.toString()}`);
}

export function fetchAdminRedemptionById(redemptionId) {
  return authorizedFetch(`/admin/redemptions/${redemptionId}`);
}

export function updateAdminRedemptionStatus(redemptionId, payload) {
  return authorizedRequest(`/admin/redemptions/${redemptionId}/status`, {
    method: "PUT",
    body: payload
  });
}
