import { useState } from "react";
import { Link } from "react-router-dom";
import AuthInput from "../../components/auth/AuthInput";
import "../../styles/AuthPages.css";
import AuthButton from "../../components/auth/AuthButton";
import AuthPageLayout from "../../components/auth/AuthPageLayout";
import AuthMessage from "../../components/auth/AuthMessage";
import { APP_ROUTES } from "../../constants/routes";


export default function LoginPage() {
    const [formValues, setFormValues] = useState({
        email: "",
        password: ""
    });

    const [errorMessage, setErrorMessage] = useState("");

    const handleInputChange = (event) => {
        const { name, value } = event.target;

        setErrorMessage("");

        setFormValues((currentValues) => ({
            ...currentValues,
            [name]: value
        }));
    };

    const handleSubmit = async (event) => {
    event.preventDefault();

    if (!formValues.email.trim() || !formValues.password.trim()) {
        setErrorMessage("Email and password are required.");
        return;
    }

    setErrorMessage("");
    console.log("Frontend-only login:", formValues);
    };

  return (
        <AuthPageLayout
            title="Admin Login"
            subtitle="Sign in to access the dashboard."
        >

        <form className="auth-form" onSubmit={handleSubmit} noValidate>
            <AuthInput
                label="Email"
                type="email"
                name="email"
                value={formValues.email}
                onChange={handleInputChange}
                placeholder="Enter your admin email"
                required
            />

            <AuthInput
                label="Password"
                type="password"
                name="password"
                value={formValues.password}
                onChange={handleInputChange}
                placeholder="Enter your password"
                required
            />

            <AuthMessage type="error">{errorMessage}</AuthMessage>

            <AuthButton type="submit">Login</AuthButton>

        </form>

        <p className="auth-subtitle auth-footer-text">
            Need an admin account? <Link to={APP_ROUTES.adminSignup}>Create one</Link>
        </p>
      </AuthPageLayout>
  );
}