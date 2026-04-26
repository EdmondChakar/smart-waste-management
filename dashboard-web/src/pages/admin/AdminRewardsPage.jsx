import { useEffect, useMemo, useState } from "react";
import AdminPageHeader from "../../components/admin/AdminPageHeader";
import AdminStatCard from "../../components/admin/AdminStatCard";
import { LIVE_REFRESH_INTERVAL_MS } from "../../constants/live";
import { fetchAdminRewards, updateAdminReward } from "../../services/adminService";
import "../../styles/AdminPages.css";

export default function AdminRewardsPage() {
  const [rewards, setRewards] = useState([]);
  const [searchTerm, setSearchTerm] = useState("");
  const [featuredFilter, setFeaturedFilter] = useState("all");
  const [isLoading, setIsLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState("");
  const [successMessage, setSuccessMessage] = useState("");
  const [updatingRewardId, setUpdatingRewardId] = useState(null);

  useEffect(() => {
    let isMounted = true;

    async function loadRewards() {
      try {
        if (isMounted) {
          setIsLoading(true);
          setErrorMessage("");
        }

        const data = await fetchAdminRewards();

        if (!isMounted) {
          return;
        }

        setRewards(data.rewards || []);
      } catch (error) {
        if (isMounted) {
          setErrorMessage(error.message || "Failed to load rewards.");
        }
      } finally {
        if (isMounted) {
          setIsLoading(false);
        }
      }
    }

    loadRewards();
    const intervalId = window.setInterval(loadRewards, LIVE_REFRESH_INTERVAL_MS);

    return () => {
      isMounted = false;
      window.clearInterval(intervalId);
    };
  }, []);

  const filteredRewards = useMemo(() => {
    const normalizedSearch = searchTerm.trim().toLowerCase();

    return rewards.filter((reward) => {
      const matchesSearch =
        !normalizedSearch ||
        reward.title.toLowerCase().includes(normalizedSearch) ||
        (reward.description || "").toLowerCase().includes(normalizedSearch);

      const matchesFeatured =
        featuredFilter === "all" ||
        (featuredFilter === "featured" && reward.is_featured) ||
        (featuredFilter === "standard" && !reward.is_featured);

      return matchesSearch && matchesFeatured;
    });
  }, [featuredFilter, rewards, searchTerm]);

  const featuredCount = rewards.filter((reward) => reward.is_featured).length;
  const activeCount = rewards.filter((reward) => reward.is_active).length;

  const handleRefresh = async () => {
    try {
      setIsLoading(true);
      setErrorMessage("");
      const data = await fetchAdminRewards();
      setRewards(data.rewards || []);
    } catch (error) {
      setErrorMessage(error.message || "Failed to refresh rewards.");
    } finally {
      setIsLoading(false);
    }
  };

  const handleToggleFeatured = async (reward) => {
    try {
      setUpdatingRewardId(reward.reward_id);
      setErrorMessage("");
      setSuccessMessage("");

      const response = await updateAdminReward(reward.reward_id, {
        title: reward.title,
        description: reward.description,
        points_cost: reward.points_cost,
        is_active: reward.is_active,
        is_featured: !reward.is_featured
      });

      setRewards((currentRewards) =>
        currentRewards.map((currentReward) =>
          currentReward.reward_id === reward.reward_id
            ? response.reward
            : currentReward
        )
      );
      setSuccessMessage(
        response.reward.is_featured
          ? `"${response.reward.title}" is now featured.`
          : `"${response.reward.title}" is no longer featured.`
      );
    } catch (error) {
      setErrorMessage(error.message || "Failed to update reward.");
    } finally {
      setUpdatingRewardId(null);
    }
  };

  return (
    <section className="admin-page-section">
      <AdminPageHeader
        eyebrow="Reward Management"
        title="Rewards"
        description="Mark rewards as featured so they appear first in the mobile Home featured section. Standard users still see the full rewards list normally."
        actions={
          <button
            className="admin-action-button admin-action-button--neutral"
            onClick={handleRefresh}
            type="button"
          >
            {isLoading ? "Refreshing..." : "Refresh"}
          </button>
        }
      />

      {errorMessage ? <p className="admin-page-error">{errorMessage}</p> : null}
      {successMessage ? (
        <p className="admin-page-success">{successMessage}</p>
      ) : null}

      <section className="admin-stats-grid">
        <AdminStatCard
          label="Total rewards"
          value={rewards.length}
          helper="Rewards currently stored in the backend."
        />
        <AdminStatCard
          label="Active rewards"
          value={activeCount}
          helper="Rewards available to users in the app."
        />
        <AdminStatCard
          label="Featured rewards"
          value={featuredCount}
          helper="Rewards currently highlighted on the mobile Home screen."
        />
      </section>

      <section className="admin-section-card">
        <div className="admin-section-header">
          <div>
            <h2>Reward Directory</h2>
            <p>Search rewards and toggle whether they are featured.</p>
          </div>
          <span className="admin-refresh-badge">
            {isLoading ? "Refreshing..." : `${filteredRewards.length} reward(s)`}
          </span>
        </div>

        <div className="admin-filter-row">
          <input
            className="admin-search-input"
            type="search"
            placeholder="Search rewards"
            value={searchTerm}
            onChange={(event) => setSearchTerm(event.target.value)}
          />

          <select
            className="admin-select-input"
            value={featuredFilter}
            onChange={(event) => setFeaturedFilter(event.target.value)}
          >
            <option value="all">All rewards</option>
            <option value="featured">Featured only</option>
            <option value="standard">Not featured</option>
          </select>
        </div>

        <div className="admin-table-wrapper">
          <table className="admin-table">
            <thead>
              <tr>
                <th>Reward</th>
                <th>Points</th>
                <th>Active</th>
                <th>Featured</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              {filteredRewards.length === 0 && !isLoading ? (
                <tr>
                  <td colSpan="5" className="admin-empty-state">
                    No rewards match the current filters.
                  </td>
                </tr>
              ) : null}

              {filteredRewards.map((reward) => (
                <tr key={reward.reward_id}>
                  <td>
                    <div className="admin-bin-cell">
                      <strong>{reward.title}</strong>
                      <span>
                        {reward.description || "No description is stored for this reward."}
                      </span>
                    </div>
                  </td>
                  <td>{reward.points_cost}</td>
                  <td>
                    <span
                      className={`admin-state-pill ${
                        reward.is_active
                          ? "admin-state-pill--safe"
                          : "admin-state-pill--muted"
                      }`}
                    >
                      {reward.is_active ? "Active" : "Inactive"}
                    </span>
                  </td>
                  <td>
                    <span
                      className={`admin-state-pill ${
                        reward.is_featured
                          ? "admin-state-pill--featured"
                          : "admin-state-pill--muted"
                      }`}
                    >
                      {reward.is_featured ? "Featured" : "Standard"}
                    </span>
                  </td>
                  <td>
                    <button
                      className={`admin-action-button ${
                        reward.is_featured
                          ? "admin-action-button--neutral"
                          : "admin-action-button--safe"
                      }`}
                      type="button"
                      disabled={updatingRewardId === reward.reward_id}
                      onClick={() => handleToggleFeatured(reward)}
                    >
                      {updatingRewardId === reward.reward_id
                        ? "Saving..."
                        : reward.is_featured
                          ? "Remove Feature"
                          : "Make Featured"}
                    </button>
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
