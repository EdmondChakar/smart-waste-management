export default function AuthButton({ type = "button", children, onClick }) {
  return (
    <button className="auth-button" type={type} onClick={onClick}>
      {children}
    </button>
  );
}