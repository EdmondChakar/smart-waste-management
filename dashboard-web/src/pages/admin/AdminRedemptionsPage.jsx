import { useEffect, useMemo, useState } from "react";
import AdminPageHeader from "../../components/admin/AdminPageHeader";
import AdminStatCard from "../../components/admin/AdminStatCard";
import { LIVE_REFRESH_INTERVAL_MS } from "../../constants/live";
import {
  fetchAdminRedemptionById,
  fetchAdminRedemptions,
  updateAdminRedemptionStatus
} from "../../services/adminService";
import { formatTimestamp } from "../../utils/adminFormatters";
import "../../styles/AdminPages.css";

const STATUS_FILTERS = [
  { value: "all", label: "All statuses" },
  { value: "REQUESTED", label: "Requested" },
  { value: "FULFILLED", label: "Fulfilled" },
  { value: "CANCELLED", label: "Cancelled" }
];

function getStatusClassName(statusCode) {
  if (statusCode === "FULFILLED") {
    return "admin-state-pill admin-state-pill--safe";
  }

  if (statusCode === "CANCELLED") {
    return "admin-state-pill admin-state-pill--alert";
  }

  return "admin-state-pill admin-state-pill--muted";
}

export default function AdminRedemptionsPage() {
  const [redemptions, setRedemptions] = useState([]);
  const [selectedRedemptionId, setSelectedRedemptionId] = useState(null);
  const [selectedRedemption, setSelectedRedemption] = useState(null);
  const [statusFilter, setStatusFilter] = useState("REQUESTED");
  const [voucherCode, setVoucherCode] = useState("");
  const [errorMessage, setErrorMessage] = useState("");
  const [actionMessage, setActionMessage] = useState("");
  const [isLoading, setIsLoading] = useState(true);
  const [isDetailLoading, setIsDetailLoading] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    let isMounted = true;

    async function loadRedemptions() {
      try {
        if (isMounted) {
          setIsLoading(true);
          setErrorMessage("");
        }

        const data = await fetchAdminRedemptions({
          statusCode: statusFilter,
          limit: 50
        });

        if (!isMounted) {
          return;
        }

        setRedemptions(data.redemptions);

        const hasSelectedRedemption = data.redemptions.some(
          (redemption) => redemption.redemption_id === selectedRedemptionId
        );

        if (data.redemptions.length === 0) {
          setSelectedRedemptionId(null);
          setSelectedRedemption(null);
        } else if (!hasSelectedRedemption) {
          setSelectedRedemptionId(data.redemptions[0].redemption_id);
        }
      } catch (error) {
        if (isMounted) {
          setErrorMessage(error.message || "Failed to load redemptions.");
        }
      } finally {
        if (isMounted) {
          setIsLoading(false);
        }
      }
    }

    loadRedemptions();
    const intervalId = window.setInterval(loadRedemptions, LIVE_REFRESH_INTERVAL_MS);

    return () => {
      isMounted = false;
      window.clearInterval(intervalId);
    };
  }, [statusFilter, selectedRedemptionId]);

  useEffect(() => {
    let isMounted = true;

    async function loadSelectedRedemption() {
      if (!selectedRedemptionId) {
        if (isMounted) {
          setSelectedRedemption(null);
        }
        return;
      }

      try {
        if (isMounted) {
          setIsDetailLoading(true);
        }

        const data = await fetchAdminRedemptionById(selectedRedemptionId);

        if (!isMounted) {
          return;
        }

        setSelectedRedemption(data);
        setVoucherCode(data.voucher_code || "");
      } catch (error) {
        if (isMounted) {
          setErrorMessage(error.message || "Failed to load redemption details.");
        }
      } finally {
        if (isMounted) {
          setIsDetailLoading(false);
        }
      }
    }

    loadSelectedRedemption();

    return () => {
      isMounted = false;
    };
  }, [selectedRedemptionId]);

  const stats = useMemo(() => {
    const requestedCount = redemptions.filter(
      (redemption) => redemption.status_code === "REQUESTED"
    ).length;
    const fulfilledCount = redemptions.filter(
      (redemption) => redemption.status_code === "FULFILLED"
    ).length;
    const cancelledCount = redemptions.filter(
      (redemption) => redemption.status_code === "CANCELLED"
    ).length;

    return {
      requestedCount,
      fulfilledCount,
      cancelledCount
    };
  }, [redemptions]);

  const canUpdateSelectedRedemption =
    selectedRedemption?.status_code === "REQUESTED" && !isSubmitting;

  async function handleStatusUpdate(nextStatusCode) {
    if (!selectedRedemption) {
      return;
    }

    try {
      setIsSubmitting(true);
      setActionMessage("");
      setErrorMessage("");

      const payload =
        nextStatusCode === "FULFILLED"
          ? {
              status_code: "FULFILLED",
              voucher_code: voucherCode.trim() || null
            }
          : {
              status_code: "CANCELLED",
              voucher_code: null
            };

      const response = await updateAdminRedemptionStatus(
        selectedRedemption.redemption_id,
        payload
      );

      setActionMessage(response.message);
      setSelectedRedemption(response.redemption);

      const listData = await fetchAdminRedemptions({
        statusCode: statusFilter,
        limit: 50
      });

      setRedemptions(listData.redemptions);

      if (
        listData.redemptions.length > 0 &&
        !listData.redemptions.some(
          (redemption) => redemption.redemption_id === selectedRedemption.redemption_id
        )
      ) {
        setSelectedRedemptionId(listData.redemptions[0].redemption_id);
      }
    } catch (error) {
      setErrorMessage(error.message || "Failed to update redemption status.");
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <section className="admin-page-section">
      <AdminPageHeader
        eyebrow="Reward Operations"
        title="Redemptions"
        description="Review citizen reward claims, fulfill approved requests, and cancel requests when points should be refunded."
      />

      {errorMessage ? <p className="admin-page-error">{errorMessage}</p> : null}
      {actionMessage ? <p className="admin-page-success">{actionMessage}</p> : null}

      <section className="admin-stats-grid">
        <AdminStatCard
          label="Visible requests"
          value={redemptions.length}
          helper="Requests returned for the current filter."
        />
        <AdminStatCard
          label="Requested"
          value={stats.requestedCount}
          helper="Redemptions waiting for admin action."
        />
        <AdminStatCard
          label="Fulfilled"
          value={stats.fulfilledCount}
          helper="Requests completed by admin staff."
        />
        <AdminStatCard
          label="Cancelled"
          value={stats.cancelledCount}
          helper="Requests cancelled with points refunded."
        />
      </section>

      <section className="admin-two-column">
        <section className="admin-section-card">
          <div className="admin-section-header">
            <div>
              <h2>Redemption Queue</h2>
              <p>Filter the queue and select a redemption to review its details.</p>
            </div>
            <span className="admin-refresh-badge">
              {isLoading ? "Refreshing..." : `${redemptions.length} item(s)`}
            </span>
          </div>

          <div className="admin-filter-row">
            <select
              className="admin-select-input"
              value={statusFilter}
              onChange={(event) => {
                setStatusFilter(event.target.value);
                setActionMessage("");
              }}
            >
              {STATUS_FILTERS.map((statusOption) => (
                <option key={statusOption.value} value={statusOption.value}>
                  {statusOption.label}
                </option>
              ))}
            </select>
          </div>

          <div className="admin-table-wrapper">
            <table className="admin-table">
              <thead>
                <tr>
                  <th>Redemption ID</th>
                  <th>User</th>
                  <th>Reward</th>
                  <th>Status</th>
                  <th>Requested</th>
                </tr>
              </thead>
              <tbody>
                {redemptions.length === 0 && !isLoading ? (
                  <tr>
                    <td colSpan="5" className="admin-empty-state">
                      No redemptions match the current filter.
                    </td>
                  </tr>
                ) : null}

                {redemptions.map((redemption) => (
                  <tr
                    key={redemption.redemption_id}
                    className={
                      selectedRedemptionId === redemption.redemption_id
                        ? "admin-table-row--selected"
                        : ""
                    }
                    onClick={() => {
                      setSelectedRedemptionId(redemption.redemption_id);
                      setActionMessage("");
                    }}
                  >
                    <td>{redemption.redemption_id}</td>
                    <td>
                      <div className="admin-bin-cell">
                        <strong>{redemption.user_email}</strong>
                        <span>User ID: {redemption.user_id}</span>
                      </div>
                    </td>
                    <td>
                      <div className="admin-bin-cell">
                        <strong>{redemption.reward_title}</strong>
                        <span>{redemption.points_spent} points</span>
                      </div>
                    </td>
                    <td>
                      <span className={getStatusClassName(redemption.status_code)}>
                        {redemption.status_code}
                      </span>
                    </td>
                    <td>{formatTimestamp(redemption.requested_at)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </section>

        <section className="admin-section-card">
          <div className="admin-section-header">
            <div>
              <h2>Selected Redemption</h2>
              <p>Inspect the request and decide whether to fulfill or cancel it.</p>
            </div>
          </div>

          {!selectedRedemptionId && !isLoading ? (
            <p className="admin-empty-note">Select a redemption to view its details.</p>
          ) : null}

          {selectedRedemption ? (
            <div className="admin-detail-stack">
              <dl className="admin-definition-list">
                <div>
                  <dt>Redemption ID</dt>
                  <dd>{selectedRedemption.redemption_id}</dd>
                </div>
                <div>
                  <dt>Status</dt>
                  <dd>
                    <span className={getStatusClassName(selectedRedemption.status_code)}>
                      {selectedRedemption.status_code}
                    </span>
                  </dd>
                </div>
                <div>
                  <dt>User</dt>
                  <dd>{selectedRedemption.user_email}</dd>
                </div>
                <div>
                  <dt>User ID</dt>
                  <dd>{selectedRedemption.user_id}</dd>
                </div>
                <div>
                  <dt>Reward</dt>
                  <dd>{selectedRedemption.reward_title}</dd>
                </div>
                <div>
                  <dt>Points Spent</dt>
                  <dd>{selectedRedemption.points_spent}</dd>
                </div>
                <div>
                  <dt>Requested At</dt>
                  <dd>{formatTimestamp(selectedRedemption.requested_at)}</dd>
                </div>
                <div>
                  <dt>Fulfilled At</dt>
                  <dd>{formatTimestamp(selectedRedemption.fulfilled_at)}</dd>
                </div>
              </dl>

              <div className="admin-action-item">
                <strong>Reward Notes</strong>
                <p>
                  {selectedRedemption.reward_description ||
                    "No reward description is stored for this item."}
                </p>
              </div>

              <div className="admin-action-item">
                <strong>Voucher Code</strong>
                <p>
                  Add a voucher or pickup code when fulfilling a request. Leave it
                  blank if the reward does not need one.
                </p>
                <div className="admin-form-row">
                  <input
                    className="admin-search-input admin-search-input--full"
                    type="text"
                    placeholder="Optional voucher code"
                    value={voucherCode}
                    onChange={(event) => setVoucherCode(event.target.value)}
                    disabled={!canUpdateSelectedRedemption}
                  />
                </div>
              </div>

              <div className="admin-action-row">
                <button
                  className="admin-action-button admin-action-button--safe"
                  type="button"
                  disabled={!canUpdateSelectedRedemption}
                  onClick={() => handleStatusUpdate("FULFILLED")}
                >
                  {isSubmitting ? "Saving..." : "Mark Fulfilled"}
                </button>
                <button
                  className="admin-action-button admin-action-button--danger"
                  type="button"
                  disabled={!canUpdateSelectedRedemption}
                  onClick={() => handleStatusUpdate("CANCELLED")}
                >
                  {isSubmitting ? "Saving..." : "Cancel & Refund"}
                </button>
              </div>

              {selectedRedemption.status_code !== "REQUESTED" ? (
                <p className="admin-data-note">
                  This redemption is already finalized. Only requests in
                  <strong> REQUESTED </strong>
                  status can be updated.
                </p>
              ) : null}
            </div>
          ) : null}

          {isDetailLoading ? (
            <p className="admin-data-note">Loading redemption details...</p>
          ) : null}
        </section>
      </section>
    </section>
  );
}
