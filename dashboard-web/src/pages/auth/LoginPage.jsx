import { useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import AuthInput from "../../components/auth/AuthInput";
import "../../styles/AuthPages.css";
import AuthButton from "../../components/auth/AuthButton";
import AuthPageLayout from "../../components/auth/AuthPageLayout";
import AuthMessage from "../../components/auth/AuthMessage";
import { APP_ROUTES } from "../../constants/routes";
import { loginAdmin } from "../../services/authService";
import { saveSession } from "../../services/sessionService";


export default function LoginPage() {
    const navigate = useNavigate();
    const location = useLocation();
    const [formValues, setFormValues] = useState({
        email: "",
        password: ""
    });

    const [errorMessage, setErrorMessage] = useState("");
    const [successMessage, setSuccessMessage] = useState("");
    const [isSubmitting, setIsSubmitting] = useState(false);

    const handleInputChange = (event) => {
        const { name, value } = event.target;

        setErrorMessage("");
        setSuccessMessage("");

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

    try {
        setIsSubmitting(true);
        setErrorMessage("");
        setSuccessMessage("");

        const data = await loginAdmin(formValues);

        saveSession({
            accessToken: data.access_token,
            user: data.user
        });

        setSuccessMessage("Login successful. Redirecting to the dashboard...");

        const nextRoute = location.state?.from || APP_ROUTES.adminDashboard;
        navigate(nextRoute, { replace: true });
    } catch (error) {
        setErrorMessage(error.message || "Login failed.");
    } finally {
        setIsSubmitting(false);
    }
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
            <AuthMessage type="success">{successMessage}</AuthMessage>

            <AuthButton type="submit" disabled={isSubmitting}>
                {isSubmitting ? "Signing In..." : "Login"}
            </AuthButton>

        </form>

        <p className="auth-subtitle auth-footer-text">
            Need an admin account? <Link to={APP_ROUTES.adminSignup}>Create one</Link>
        </p>
      </AuthPageLayout>
  );
}
