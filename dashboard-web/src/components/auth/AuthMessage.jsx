export default function AuthMessage({ children, type = "error" }) {
  if (!children) {
    return null;
  }

  const className = `auth-message auth-message--${type}`;

  return <p className={className}>{children}</p>;
}