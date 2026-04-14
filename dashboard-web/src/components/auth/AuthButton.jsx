export default function AuthButton({
  type = "button",
  children,
  onClick,
  disabled = false
}) {
  return (
    <button
      className="auth-button"
      type={type}
      onClick={onClick}
      disabled={disabled}
    >
      {children}
    </button>
  );
}
