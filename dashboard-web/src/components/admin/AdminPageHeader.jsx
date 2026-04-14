export default function AdminPageHeader({
  eyebrow,
  title,
  description,
  actions = null
}) {
  return (
    <div className="admin-page-heading">
      <div>
        {eyebrow ? <p className="admin-page-eyebrow">{eyebrow}</p> : null}
        <h1>{title}</h1>
        {description ? <p>{description}</p> : null}
      </div>

      {actions ? <div className="admin-page-actions">{actions}</div> : null}
    </div>
  );
}
