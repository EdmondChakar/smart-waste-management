import { useState } from "react";
import { Link } from "react-router-dom";
import AuthInput from "../../components/auth/AuthInput";
import AuthButton from "../../components/auth/AuthButton";
import AuthPageLayout from "../../components/auth/AuthPageLayout";
import AuthMessage from "../../components/auth/AuthMessage";
import "../../styles/AuthPages.css";
import { APP_ROUTES } from "../../constants/routes";

export default function SignupPage() {
    const [formValues, setFormValues] = useState({
        fullName: "",
        email: "",
        password: "",
        confirmPassword: "",
        adminCode: ""
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

    const handleSubmit = (event) => {
        event.preventDefault();

        const { fullName, email, password, confirmPassword, adminCode } = formValues;

        if (
            !fullName.trim() ||
            !email.trim() ||
            !password.trim() ||
            !confirmPassword.trim() ||
            !adminCode.trim()
        ) {
            setErrorMessage("All fields are required.");
            return;
        }

        if (password !== confirmPassword) {
            setErrorMessage("Passwords do not match.");
            return;
        }

        setErrorMessage("");
        console.log("Signup form submitted:", formValues);
    };

  return (
    <AuthPageLayout
        title="Admin Signup"
        subtitle="Create an admin account for the dashboard."
    >
        <form className="auth-form" onSubmit={handleSubmit} noValidate>
        <AuthInput
            label="Full Name"
            name="fullName"
            value={formValues.fullName}
            onChange={handleInputChange}
            placeholder="Enter your full name"
            required
        />

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

        <AuthInput
            label="Confirm Password"
            type="password"
            name="confirmPassword"
            value={formValues.confirmPassword}
            onChange={handleInputChange}
            placeholder="Re-enter your password"
            required
        />

        <AuthInput
            label="Admin Code"
            name="adminCode"
            value={formValues.adminCode}
            onChange={handleInputChange}
            placeholder="Enter the admin code"
            required
        />

        <AuthMessage type="error">{errorMessage}</AuthMessage>

        <AuthButton type="submit">Create Account</AuthButton>
        </form>

        <p className="auth-subtitle auth-footer-text">
        Already have an account? <Link to={APP_ROUTES.adminLogin}>Login</Link>
        </p>
    </AuthPageLayout>
    );
}