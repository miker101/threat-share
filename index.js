
"use strict";

document.addEventListener("DOMContentLoaded", () => {
    const sidebar = document.getElementById("sidebar");
    const menuToggle = document.getElementById("menuToggle");
    const sidebarOverlay = document.getElementById("sidebarOverlay");
    const refreshButton = document.getElementById("refreshButton");
    const currentDate = document.getElementById("currentDate");
    const footerYear = document.getElementById("footerYear");
    const connectionStatus = document.getElementById("connectionStatus");

    const pendingCountElement =
        document.getElementById("pendingOrganizationsCount");
    const organizationsCountElement =
        document.getElementById("organizationsCount");
    const pendingBadge = document.getElementById("pendingOrgBadge");

    const API_BASE_URL = "http://localhost:8080/api/admin";
    const TOKEN_KEY = "threatshareAdminToken";

    function updateDate() {
        if (!currentDate) return;

        currentDate.textContent = new Intl.DateTimeFormat("en-GB", {
            day: "2-digit",
            month: "short",
            year: "numeric"
        }).format(new Date());
    }

    function updateFooterYear() {
        if (footerYear) {
            footerYear.textContent = new Date().getFullYear();
        }
    }

    function openSidebar() {
        if (!sidebar || !menuToggle || !sidebarOverlay) return;

        sidebar.classList.add("sidebar-open");
        sidebarOverlay.classList.add("overlay-visible");
        menuToggle.setAttribute("aria-expanded", "true");
        document.body.style.overflow = "hidden";
    }

    function closeSidebar() {
        if (!sidebar || !menuToggle || !sidebarOverlay) return;

        sidebar.classList.remove("sidebar-open");
        sidebarOverlay.classList.remove("overlay-visible");
        menuToggle.setAttribute("aria-expanded", "false");
        document.body.style.overflow = "";
    }

    if (menuToggle) {
        menuToggle.addEventListener("click", () => {
            if (sidebar?.classList.contains("sidebar-open")) {
                closeSidebar();
            } else {
                openSidebar();
            }
        });
    }

    sidebarOverlay?.addEventListener("click", closeSidebar);

    sidebar?.querySelectorAll(".nav-link").forEach((link) => {
        link.addEventListener("click", closeSidebar);
    });

    document.addEventListener("keydown", (event) => {
        if (event.key === "Escape") closeSidebar();
    });

    function setConnectionStatus(message, title) {
        if (!connectionStatus) return;

        connectionStatus.textContent = message;
        connectionStatus.title = title || "";
    }

   
async function fetchWithToken(url, token, options = {}) {
    return fetch(url, {
        method: options.method || "GET",
        headers: {
            "Authorization": `Bearer ${token}`,
            "Accept": "application/json",
            ...options.headers
        },
        body: options.body
    });
}



async function loadTotalOrganizations(token) {
        const response = await fetchWithToken(
            `${API_BASE_URL}/organizations/count`,
            token
        );

        if (response.status === 401) {
    throw new Error("AUTHENTICATION_FAILED");
}

if (response.status === 403) {
    throw new Error(
        "ACCESS_FORBIDDEN: The server denied this request."
    );
}

        if (!response.ok) {
            throw new Error(
                `Organization count request failed: HTTP ${response.status}`
            );
        }

        const count = await response.json();

        if (typeof count !== "number" || !Number.isFinite(count)) {
            throw new Error("Unexpected organization count response.");
        }

        if (organizationsCountElement) {
            organizationsCountElement.textContent =
                count.toLocaleString();
        }
    }

    async function loadPendingReviews(token) {
        const response = await fetchWithToken(
            `${API_BASE_URL}/organizations/pending`,
            token
        );

        if (response.status === 401) {
    throw new Error("AUTHENTICATION_FAILED");
}

if (response.status === 403) {
    throw new Error(
        "ACCESS_FORBIDDEN: The server denied this request."
    );
}

        if (!response.ok) {
            throw new Error(
                `Pending reviews request failed: HTTP ${response.status}`
            );
        }

        const organizations = await response.json();

        if (!Array.isArray(organizations)) {
            throw new Error("Unexpected pending-organizations response.");
        }

        const count = organizations.length;

        if (pendingCountElement) {
            pendingCountElement.textContent = count.toLocaleString();
        }

        if (pendingBadge) {
            pendingBadge.textContent = count.toLocaleString();
            pendingBadge.hidden = count === 0;
        }

        console.info(
            `ThreatShare: loaded ${count} pending organization reviews.`
        );
    }

    async function loadDashboardData() {
        const token = sessionStorage.getItem(TOKEN_KEY);

       if (!token) {
    setConnectionStatus(
        "Authentication required",
        "No token found in sessionStorage."
    );

    console.error(
        "DEBUG: No token found under:",
        TOKEN_KEY
    );

    return; // Temporarily stop redirecting.
}

        if (organizationsCountElement) {
            organizationsCountElement.textContent = "…";
        }

        if (pendingCountElement) {
            pendingCountElement.textContent = "…";
        }

        if (pendingBadge) {
            pendingBadge.hidden = true;
        }

        setConnectionStatus("Connecting…");

        try {
            // Load both cards independently so one failure
            // does not prevent the other card from updating.
            const results = await Promise.allSettled([
                loadTotalOrganizations(token),
                loadPendingReviews(token)
            ]);

            const authFailed = results.some(
                result =>
                    result.status === "rejected" &&
                    result.reason?.message === "AUTHENTICATION_FAILED"
            );

            if (authFailed) {
    console.error(
        "DEBUG: An admin API returned HTTP 401 or 403.",
        results
    );

    setConnectionStatus(
        "Authentication failed — DEBUG MODE"
    );

    return; // Temporarily stop redirecting.
}

            const failures = results.filter(
                result => result.status === "rejected"
            );

            failures.forEach(result => {
                console.error(
                    "Dashboard data loading error:",
                    result.reason
                );
            });

            if (failures.length === 0) {
                setConnectionStatus(
                    "API connected",
                    "Dashboard data loaded successfully."
                );
            } else if (failures.length === results.length) {
                setConnectionStatus(
                    "Connection failed",
                    "Could not load dashboard data. Check the backend and network requests."
                );
            } else {
                setConnectionStatus(
                    "Partially connected",
                    "Some dashboard data could not be loaded."
                );
            }
        } catch (error) {
            console.error("Dashboard error:", error);
            setConnectionStatus("Connection failed");
        }
    }

    if (refreshButton) {
        refreshButton.addEventListener("click", async () => {
            updateDate();
            refreshButton.disabled = true;

            try {
                await loadDashboardData();
            } finally {
                refreshButton.disabled = false;
            }
        });
    }

    updateDate();
    updateFooterYear();
    loadDashboardData();
});
