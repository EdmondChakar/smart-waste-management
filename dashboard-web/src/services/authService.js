const AUTH_API_BASE_URL = "http://localhost:5000/api/admin/auth";

export async function loginAdmin(credentials) {
    const response = await fetch(`${AUTH_API_BASE_URL}/login`, {
        method: "POST",
        headers: {
        "Content-Type": "application/json"
        },
        body: JSON.stringify(credentials)
    });

    const data = await response.json();

    if (!response.ok) {
        throw new Error(data.message || "Login failed.");
    }

    return data;
}

export async function signupAdmin(payload) {
    const response = await fetch(`${AUTH_API_BASE_URL}/signup`, {
        method: "POST",
        headers: {
        "Content-Type": "application/json"
        },
        body: JSON.stringify(payload)
    });

    const data = await response.json();

    if (!response.ok) {
        throw new Error(data.message || "Signup failed.");
    }

    return data;
}