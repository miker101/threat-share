
package com.example.demo.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
public class OrganizationAccessService {

    private final CurrentUserService currentUserService;

    public OrganizationAccessService(
            CurrentUserService currentUserService) {
        this.currentUserService = currentUserService;
    }

    public void verifyOrganizationAccess(Long requestedOrganizationId) {

        Long authenticatedOrganizationId =
                currentUserService.getCurrentOrganizationId();

        if (!authenticatedOrganizationId.equals(requestedOrganizationId)) {
            throw new AccessDeniedException(
                    "You do not have permission to access this organization's data."
            );
        }
    }
}
