export default function AdminStatCard({ label, value, helper }) {
  return (
    <article className="admin-stat-card">
      <span className="admin-stat-label">{label}</span>
      <strong>{value}</strong>
      {helper ? <p>{helper}</p> : null}
    </article>
  );
}
