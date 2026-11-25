"use strict";

document.addEventListener("DOMContentLoaded", () => {
    const API_BASE_URL = "http://localhost:8080/api/admin";
    const TOKEN_KEY = "threatshareAdminToken";
    const EXPIRY_KEY = "threatshareAdminTokenExpiresAt";

    const $ = (id) => document.getElementById(id);

    const elements = {
        sidebar: $("sidebar"),
        menuToggle: $("menuToggle"),
        sidebarOverlay: $("sidebarOverlay"),
        connectionStatus: $("connectionStatus"),
        currentDate: $("currentDate"),
        footerYear: $("footerYear"),
        refreshButton: $("refreshButton"),

        totalCount: $("organizationsCount"),
        pendingCount: $("pendingOrganizationsCount"),
        activeCount: $("activeOrganizationsCount"),
        rejectedCount: $("rejectedOrganizationsCount"),
        pendingBadge: $("pendingOrgBadge"),

        search: $("organizationSearch"),
        statusFilter: $("statusFilter"),
        tableBody: $("organizationsTableBody"),
        resultsCount: $("resultsCount"),
        pageMessage: $("pageMessage"),

        reviewPanel: $("reviewPanel"),
        reviewName: $("reviewOrganizationName"),
        detailOrganizationName: $("detailOrganizationName"),
        detailOrganizationEmail: $("detailOrganizationEmail"),
        detailAdminName: $("detailAdminName"),
        detailAdminEmail: $("detailAdminEmail"),
        detailRegisteredAt: $("detailRegisteredAt"),
        detailStatus: $("detailOrganizationStatus"),
        reviewActions: $("reviewActions"),
        reviewMessage: $("reviewMessage"),
        closeReviewButton: $("closeReviewButton"),
        approveButton: $("approveOrganizationButton"),
        rejectButton: $("rejectOrganizationButton")
    };

    let pendingOrganizations = [];
    let selectedOrganization = null;
    let actionInProgress = false;

    function setConnectionStatus(message) {
        if (elements.connectionStatus) {
            elements.connectionStatus.textContent = message;
        }
    }

    function showMessage(element, message, type = "") {
        if (!element) return;

        element.textContent = message;
        element.className = "page-message";

        if (type) {
            element.classList.add(`message-${type}`);
        }

        element.hidden = false;
    }

    function hideMessage(element) {
        if (!element) return;

        element.textContent = "";
        element.hidden = true;
        element.className = "page-message";
    }

    function escapeHTML(value) {
        return String(value ?? "").replace(/[&<>"']/g, (character) => ({
            "&": "&amp;",
            "<": "&lt;",
            ">": "&gt;",
            '"': "&quot;",
            "'": "&#39;"
        })[character]);
    }

    function formatDate(value) {
        if (!value) return "—";

        const date = new Date(value);

        if (Number.isNaN(date.getTime())) {
            return "—";
        }

        return new Intl.DateTimeFormat("en-GB", {
            day: "2-digit",
            month: "short",
            year: "numeric",
            hour: "2-digit",
            minute: "2-digit"
        }).format(date);
    }

    function statusClass(status) {
        switch (String(status || "").toUpperCase()) {
            case "ACTIVE":
                return "status-active";
            case "REJECTED":
                return "status-rejected";
            default:
                return "status-pending";
        }
    }

    function renderTable() {
        const searchTerm = elements.search.value.trim().toLowerCase();
        const selectedStatus = elements.statusFilter.value;

        const filtered = pendingOrganizations.filter((organization) => {
            const searchableText = [
                organization.organizationName,
                organization.organizationEmail,
                organization.adminFullName,
                organization.adminEmail,
                organization.organizationStatus
            ].join(" ").toLowerCase();

            const matchesSearch = searchableText.includes(searchTerm);
            const matchesStatus = selectedStatus === "ALL" ||
                String(organization.organizationStatus).toUpperCase() === selectedStatus;

            return matchesSearch && matchesStatus;
        });

        if (elements.resultsCount) {
            elements.resultsCount.textContent =
                `${filtered.length} pending application${filtered.length === 1 ? "" : "s"}`;
        }

        if (filtered.length === 0) {
            elements.tableBody.innerHTML = `
                <tr>
                    <td colspan="5" class="table-placeholder">
                        No pending applications match your search.
                    </td>
                </tr>`;
            return;
        }

        elements.tableBody.innerHTML = filtered.map((organization) => `
            <tr>
                <td>
                    <span class="organization-name">
                        ${escapeHTML(organization.organizationName)}
                    </span>
                    <span class="organization-email">
                        ${escapeHTML(organization.organizationEmail)}
                    </span>
                </td>
                <td>
                    <span class="admin-name">
                        ${escapeHTML(organization.adminFullName)}
                    </span>
                    <span class="admin-email">
                        ${escapeHTML(organization.adminEmail)}
                    </span>
                </td>
                <td>${escapeHTML(formatDate(organization.registeredAt))}</td>
                <td>
                    <span class="status-pill ${statusClass(organization.organizationStatus)}">
                        ${escapeHTML(organization.organizationStatus)}
                    </span>
                </td>
                <td>
                    <button
                        type="button"
                        class="review-button"
                        data-review-id="${escapeHTML(organization.organizationId)}">
                        Review
                    </button>
                </td>
            </tr>
        `).join("");
    }

    async function apiRequest(path, options = {}) {
        const token = sessionStorage.getItem(TOKEN_KEY);

        if (!token) {
            throw new Error("AUTHENTICATION_REQUIRED");
        }

        const response = await fetch(`${API_BASE_URL}${path}`, {
            method: options.method || "GET",
            headers: {
                "Authorization": `Bearer ${token}`,
                "Accept": "application/json",
                ...(options.headers || {})
            },
            ...(options.body !== undefined ? { body: options.body } : {})
        });

       if (response.status === 401) {
    throw new Error("AUTHENTICATION_FAILED");
}

if (response.status === 403) {
    throw new Error(
        "ACCESS_FORBIDDEN: You do not have permission to access this endpoint."
    );
}

        let data = null;

        try {
            data = await response.json();
        } catch {
            // Some responses may not contain JSON.
        }

        if (!response.ok) {
            throw new Error(
                data?.detail ||
                data?.message ||
                `Request failed (HTTP ${response.status}).`
            );
        }

        return data;
    }

    function handleAuthenticationError(error) {
        if (
            error.message === "AUTHENTICATION_REQUIRED" ||
            error.message === "AUTHENTICATION_FAILED"
        ) {
            sessionStorage.removeItem(TOKEN_KEY);
            sessionStorage.removeItem(EXPIRY_KEY);
            window.location.replace("../login.html");
            return true;
        }

        return false;
    }

    async function loadOrganizations() {
        setConnectionStatus("Loading…");

        if (elements.tableBody) {
            elements.tableBody.innerHTML = `
                <tr>
                    <td colspan="5" class="table-placeholder">
                        Loading pending applications…
                    </td>
                </tr>`;
        }

        try {
const [countsResult, pendingResult] = await Promise.allSettled([
    apiRequest("/organizations/counts"),
    apiRequest("/organizations/pending")
]);

            const rejectedResults = [countsResult, pendingResult]
    .filter((result) => result.status === "rejected");

            const authError = rejectedResults.find((result) =>
                handleAuthenticationError(result.reason)
            );

            if (authError) return;

            if (countsResult.status === "fulfilled") {
    const counts = countsResult.value;

    elements.totalCount.textContent =
        Number(counts.total).toLocaleString();

    elements.pendingCount.textContent =
        Number(counts.pending).toLocaleString();

    elements.activeCount.textContent =
        Number(counts.active).toLocaleString();

    elements.rejectedCount.textContent =
        Number(counts.rejected).toLocaleString();
} else {
    console.error("Could not load organization counts:", countsResult.reason);

    elements.totalCount.textContent = "—";
    elements.pendingCount.textContent = "—";
    elements.activeCount.textContent = "—";
    elements.rejectedCount.textContent = "—";
}

            if (pendingResult.status === "fulfilled") {
                pendingOrganizations = Array.isArray(pendingResult.value)
                    ? pendingResult.value
                    : [];

                const pendingCount = pendingOrganizations.length;

                elements.pendingCount.textContent = pendingCount.toLocaleString();
                elements.pendingBadge.textContent = pendingCount.toLocaleString();
                elements.pendingBadge.hidden = pendingCount === 0;

                renderTable();
            } else {
                console.error("Could not load pending organizations:", pendingResult.reason);
                pendingOrganizations = [];
                renderTable();

                showMessage(
                    elements.pageMessage,
                    "Could not load pending applications. Check the backend response and browser console.",
                    "error"
                );
            }

           

            if (rejectedResults.length === 0) {
                setConnectionStatus("API connected");
                hideMessage(elements.pageMessage);
            } else {
                setConnectionStatus("Partially connected");
            }
        } catch (error) {
            console.error("Organization page loading error:", error);

            if (handleAuthenticationError(error)) return;

            setConnectionStatus("Connection failed");
            showMessage(
                elements.pageMessage,
                error.message || "Unable to connect to the backend.",
                "error"
            );

            elements.tableBody.innerHTML = `
                <tr>
                    <td colspan="5" class="table-placeholder">
                        Unable to load organization data.
                    </td>
                </tr>`;
        }
    }

    function openReview(organization) {
        selectedOrganization = organization;

        elements.reviewName.textContent = organization.organizationName || "Organization review";
        elements.detailOrganizationName.textContent = organization.organizationName || "—";
        elements.detailOrganizationEmail.textContent = organization.organizationEmail || "—";
        elements.detailAdminName.textContent = organization.adminFullName || "—";
        elements.detailAdminEmail.textContent = organization.adminEmail || "—";
        elements.detailRegisteredAt.textContent = formatDate(organization.registeredAt);
        elements.detailStatus.textContent = organization.organizationStatus || "—";

        const isPending =
            String(organization.organizationStatus).toUpperCase() === "PENDING";

        elements.reviewActions.hidden = !isPending;
        hideMessage(elements.reviewMessage);

        elements.reviewPanel.hidden = false;
        elements.reviewPanel.scrollIntoView({
            behavior: "smooth",
            block: "start"
        });
    }

    async function performDecision(decision) {
        if (!selectedOrganization || actionInProgress) return;

        const organization = selectedOrganization;
        const organizationId = organization.organizationId;
        const isApproval = decision === "approve";

        const confirmationMessage = isApproval
            ? `Approve "${organization.organizationName}" and activate its pending administrator account(s)?`
            : `Reject "${organization.organizationName}" and reject its pending administrator account(s)?`;

        if (!window.confirm(confirmationMessage)) return;

        actionInProgress = true;
        elements.approveButton.disabled = true;
        elements.rejectButton.disabled = true;

        elements.approveButton.textContent = isApproval
            ? "Approving…"
            : "Approve organization";
        elements.rejectButton.textContent = isApproval
            ? "Reject application"
            : "Rejecting…";

        hideMessage(elements.reviewMessage);

        try {
            const result = await apiRequest(
                `/organizations/${encodeURIComponent(organizationId)}/${decision}`,
                { method: "PATCH" }
            );

            showMessage(
                elements.reviewMessage,
                result?.message || `Organization ${decision}d successfully.`,
                "success"
            );

            await loadOrganizations();

            // Close the panel after a successful state change.
            elements.reviewPanel.hidden = true;
            selectedOrganization = null;
        } catch (error) {
            console.error(`Organization ${decision} failed:`, error);

            if (!handleAuthenticationError(error)) {
                showMessage(
                    elements.reviewMessage,
                    error.message || `Could not ${decision} this organization.`,
                    "error"
                );
            }
        } finally {
            actionInProgress = false;
            elements.approveButton.disabled = false;
            elements.rejectButton.disabled = false;
            elements.approveButton.textContent = "Approve organization";
            elements.rejectButton.textContent = "Reject application";
        }
    }

    elements.tableBody.addEventListener("click", (event) => {
        const button = event.target.closest("[data-review-id]");

        if (!button) return;

        const organization = pendingOrganizations.find(
            (item) => String(item.organizationId) === button.dataset.reviewId
        );

        if (organization) {
            openReview(organization);
        }
    });

    elements.search.addEventListener("input", renderTable);
    elements.statusFilter.addEventListener("change", renderTable);

    elements.refreshButton.addEventListener("click", async () => {
        elements.refreshButton.disabled = true;

        try {
            await loadOrganizations();
        } finally {
            elements.refreshButton.disabled = false;
        }
    });

    elements.closeReviewButton.addEventListener("click", () => {
        elements.reviewPanel.hidden = true;
        selectedOrganization = null;
    });

    elements.approveButton.addEventListener("click", () => {
        performDecision("approve");
    });

    elements.rejectButton.addEventListener("click", () => {
        performDecision("reject");
    });

    elements.menuToggle?.addEventListener("click", () => {
        const isOpen = elements.sidebar.classList.toggle("sidebar-open");

        elements.sidebarOverlay.classList.toggle("overlay-visible", isOpen);
        elements.menuToggle.setAttribute("aria-expanded", String(isOpen));
        document.body.style.overflow = isOpen ? "hidden" : "";
    });

    elements.sidebarOverlay?.addEventListener("click", () => {
        elements.sidebar.classList.remove("sidebar-open");
        elements.sidebarOverlay.classList.remove("overlay-visible");
        elements.menuToggle?.setAttribute("aria-expanded", "false");
        document.body.style.overflow = "";
    });

    elements.sidebar?.querySelectorAll(".nav-link").forEach((link) => {
        link.addEventListener("click", () => {
            elements.sidebar.classList.remove("sidebar-open");
            elements.sidebarOverlay?.classList.remove("overlay-visible");
            elements.menuToggle?.setAttribute("aria-expanded", "false");
            document.body.style.overflow = "";
        });
    });

    document.addEventListener("keydown", (event) => {
        if (event.key === "Escape" && !elements.reviewPanel.hidden) {
            elements.reviewPanel.hidden = true;
            selectedOrganization = null;
        }
    });

    if (elements.currentDate) {
        elements.currentDate.textContent = new Intl.DateTimeFormat("en-GB", {
            day: "2-digit",
            month: "short",
            year: "numeric"
        }).format(new Date());
    }

    if (elements.footerYear) {
        elements.footerYear.textContent = new Date().getFullYear();
    }

    loadOrganizations();
});