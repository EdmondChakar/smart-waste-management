export default function AuthInput({
  label,
  type = "text",
  name,
  value,
  onChange,
  placeholder,
  required = false
}) {
  return (
    <div className="auth-field">
      <label className="auth-label" htmlFor={name}>
        {label}
      </label>

      <input
        id={name}
        className="auth-input"
        type={type}
        name={name}
        value={value}
        onChange={onChange}
        placeholder={placeholder}
        required={required}
      />
    </div>
  );
}