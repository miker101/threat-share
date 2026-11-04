
"use strict";

/* =========================================
   THREATSHARE ADMIN — LOGIN PAGE
   Connected to the Spring Boot backend.
   ========================================= */

document.addEventListener("DOMContentLoaded", () => {
    const API_BASE_URL = "http://localhost:8080/api/admin";

    const TOKEN_KEY = "threatshareAdminToken";
    const EXPIRY_KEY = "threatshareAdminTokenExpiresAt";

    const loginForm = document.getElementById("loginForm");
    const emailInput = document.getElementById("email");
    const passwordInput = document.getElementById("password");

    const emailError = document.getElementById("emailError");
    const passwordError = document.getElementById("passwordError");

    const passwordToggle = document.getElementById("passwordToggle");
    const loginMessage = document.getElementById("loginMessage");
    const loginButton = document.getElementById("loginButton");
    const currentYear = document.getElementById("currentYear");

    if (currentYear) {
        currentYear.textContent = new Date().getFullYear();
    }

    // Redirect to the dashboard if the password visibility button is clicked.
    if (passwordToggle && passwordInput) {
        passwordToggle.addEventListener("click", () => {
            const showPassword = passwordInput.type === "password";

            passwordInput.type = showPassword ? "text" : "password";

            passwordToggle.setAttribute(
                "aria-label",
                showPassword ? "Hide password" : "Show password"
            );

            passwordToggle.setAttribute(
                "aria-pressed",
                String(showPassword)
            );
        });
    }

    function clearError(input, errorElement) {
        if (errorElement) {
            errorElement.textContent = "";
        }

        if (input) {
            input.closest(".input-wrapper")
                ?.classList.remove("input-invalid");

            input.removeAttribute("aria-invalid");
        }
    }

    function showError(input, errorElement, message) {
        if (errorElement) {
            errorElement.textContent = message;
        }

        if (input) {
            input.closest(".input-wrapper")
                ?.classList.add("input-invalid");

            input.setAttribute("aria-invalid", "true");
        }
    }

    function showMessage(message, type = "") {
        if (!loginMessage) return;

        loginMessage.textContent = message;
        loginMessage.className = "login-message";

        if (type) {
            loginMessage.classList.add('message-${type}');
}


        loginMessage.hidden = false;
    }

    function clearMessage() {
        if (!loginMessage) return;

        loginMessage.textContent = "";
        loginMessage.className = "login-message";
        loginMessage.hidden = true;
    }

    // Read a JSON response safely, including error responses.
    async function readResponse(response) {
        try {
            return await response.json();
        } catch {
            return {};
        }
    }

    // Remove an existing frontend token when login cannot be completed.
    function clearStoredAuthentication() {
        sessionStorage.removeItem(TOKEN_KEY);
        sessionStorage.removeItem(EXPIRY_KEY);
    }

    if (emailInput) {
        emailInput.addEventListener("input", () => {
            clearError(emailInput, emailError);
            clearMessage();
        });
    }

    if (passwordInput) {
        passwordInput.addEventListener("input", () => {
            clearError(passwordInput, passwordError);
            clearMessage();
        });
    }

    if (!loginForm || !emailInput || !passwordInput) {
        console.error("ThreatShare login form elements are missing.");
        return;
    }

    loginForm.addEventListener("submit", async (event) => {
        event.preventDefault();

        clearError(emailInput, emailError);
        clearError(passwordInput, passwordError);
        clearMessage();

        const email = emailInput.value.trim();
        const password = passwordInput.value;

        let isValid = true;

        if (!email) {
            showError(
                emailInput,
                emailError,
                "Enter your email address."
            );
            isValid = false;
        } else if (
            email.length > 254 ||
            !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)
        ) {
            showError(
                emailInput,
                emailError,
                "Enter a valid email address."
            );
            isValid = false;
        }

        if (!password) {
            showError(
                passwordInput,
                passwordError,
                "Enter your password."
            );
            isValid = false;
        }

        if (!isValid) {
            loginForm
                .querySelector('[aria-invalid="true"]')
                ?.focus();

            return;
        }

        const originalButtonContent = loginButton
            ? loginButton.innerHTML
            : "";

        if (loginButton) {
            loginButton.disabled = true;
            loginButton.textContent = "Signing in...";
        }

        try {
            // Step 1: Authenticate with the real backend.
            const loginResponse = await fetch(
                `${API_BASE_URL}/auth/login`,
                {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json"
                    },
                    body: JSON.stringify({
                        email: email,
                        password: password
                    })
                }
            );

            const loginData = await readResponse(loginResponse);

            if (!loginResponse.ok) {
                if (loginResponse.status === 401) {
                    showMessage(
                        "Invalid email or password, or this account is inactive.",
                        "error"
                    );
                } else if (loginResponse.status === 400) {
                    showMessage(
                        "The login details were rejected. Check your input and try again.",
                        "error"
                    );
                } else {
                    showMessage(
                        loginData.detail ||
                        loginData.message ||
                        `Login failed (HTTP ${loginResponse.status}).`,
                        "error"
                    );
                }

                return;
            }

            if (
                typeof loginData.token !== "string" ||
                !loginData.token ||
                loginData.tokenType !== "Bearer"
            ) {
                showMessage(
                    "The server response did not contain a valid Bearer token.",
                    "error"
                );
                return;
            }

            // Step 2: Verify the token can access a protected admin endpoint.
            // This checks that the JWT filter authenticates the request
            // and grants the PLATFORM_ADMIN authority.
            const verificationResponse = await fetch(
                `${API_BASE_URL}/organizations/pending`,
                {
                    method: "GET",
                    headers: {
                        "Authorization": `Bearer ${loginData.token}`,
                        "Accept": "application/json"
                    }
                }
            );

            if (!verificationResponse.ok) {
                clearStoredAuthentication();

                if (
                    verificationResponse.status === 401 ||
                    verificationResponse.status === 403
                ) {
                    showMessage(
                        "Login credentials were accepted, but access to the protected admin API was denied. Check your JWT filter and platform-admin role configuration.",
                        "error"
                    );
                } else {
                    showMessage(
                        `The token was issued, but admin access verification failed (HTTP ${verificationResponse.status}).`,
                        "error"
                    );
                }

                return;
            }

            // Step 3: Store the token only after protected access succeeds.
            const expiresInSeconds =
                Number.parseInt(loginData.expiresIn, 10);

            const expiryTime = Number.isFinite(expiresInSeconds)
                ? Date.now() + expiresInSeconds * 1000
                : Date.now() + 3600 * 1000;

            sessionStorage.setItem(TOKEN_KEY, loginData.token);
            sessionStorage.setItem(
                EXPIRY_KEY,
                String(expiryTime)
            );

            // Do not store the password.
            window.location.replace("index.html");

        } catch (error) {
            console.error("ThreatShare login error:", error);

            clearStoredAuthentication();

            showMessage(
                "Could not connect to the backend. Make sure Spring Boot is running on port 8080 and that its CORS configuration allows your frontend origin.",
                "error"
            );
        } finally {
            if (loginButton) {
                loginButton.disabled = false;
                loginButton.innerHTML = originalButtonContent;
            }
        }
    });
});
