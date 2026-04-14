import { buildApiUrl } from "./api";

export async function loginAdmin(credentials) {
    const response = await fetch(buildApiUrl("/admin/login"), {
        method: "POST",
        headers: {
        "Content-Type": "application/json"
        },
        body: JSON.stringify(credentials)
    });

    const data = await response.json();

    if (!response.ok) {
        throw new Error(data.detail || data.message || "Login failed.");
    }

    return data;
}

export async function signupAdmin(payload) {
    const response = await fetch(buildApiUrl("/admin/signup"), {
        method: "POST",
        headers: {
        "Content-Type": "application/json"
        },
        body: JSON.stringify(payload)
    });

    const data = await response.json();

    if (!response.ok) {
        throw new Error(data.detail || data.message || "Signup failed.");
    }

    return data;
}
