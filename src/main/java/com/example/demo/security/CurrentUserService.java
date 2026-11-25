
package com.example.demo.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    // Get authenticated Organization Admin
    public OrganizationAdminPrincipal getCurrentOrganizationAdmin() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !(authentication.getPrincipal()
                        instanceof OrganizationAdminPrincipal principal)) {

            throw new AccessDeniedException(
                    "Organization Admin access is required."
            );
        }

        return principal;
    }

    // Get authenticated Platform Admin
    public PlatformAdminPrincipal getCurrentPlatformAdmin() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !(authentication.getPrincipal()
                        instanceof PlatformAdminPrincipal principal)) {

            throw new AccessDeniedException(
                    "Platform Admin access is required."
            );
        }

        return principal;
    }

    public Long getCurrentOrganizationId() {
        return getCurrentOrganizationAdmin().organizationId();
    }

    public Long getCurrentUserId() {
        return getCurrentOrganizationAdmin().userId();
    }

    public Long getCurrentPlatformAdminId() {
    	return getCurrentPlatformAdmin().adminId();
    }
}
