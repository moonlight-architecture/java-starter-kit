package com.servicecops.project.services;

import com.jet.moonlight.security.JetSecurityContext;
import com.jet.moonlight.services.JetService;
import com.servicecops.project.models.database.SystemRoleModel;
import com.servicecops.project.models.database.SystemUserModel;
import com.servicecops.project.models.jpahelpers.enums.AppDomains;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Domain helpers for Jet services. Auth checks prefer {@link JetSecurityContext}
 * and the principal already loaded by {@code JwtFilter} — no extra role queries.
 */
public abstract class UniversalService extends JetService {
    @Autowired
    private JetSecurityContext jetSecurityContext;

    /**
     * Checks if the user has access to a certain domain
     * @param domain AppDomains - The domain in quest
     */
    public void belongsTo(AppDomains domain) {
        if (getUserDomain() != domain) {
            throw new IllegalStateException("You have no access to the " + domain + " services");
        }
    }

    /**
     * Returns all the permissions of the logged-in user (from the principal).
     */
    public List<String> userPerms() {
        List<String> perms = new ArrayList<>();
        for (GrantedAuthority authority : authenticatedUser().getAuthorities()) {
            perms.add(authority.getAuthority());
        }
        return perms;
    }

    /**
     * The domain of the currently logged-in user (from the principal transient).
     */
    public AppDomains getUserDomain() {
        return authenticatedUser().getRoleDomain();
    }

    /**
     * Role view built from the principal — no database round-trip.
     */
    public SystemRoleModel getRole() {
        SystemUserModel user = authenticatedUser();
        return SystemRoleModel.builder()
                .roleCode(user.getRoleCode())
                .roleName(user.getRoleName())
                .roleDomain(user.getRoleDomain())
                .build();
    }

    /**
     * @return true when the logged-in user has the given role code
     */
    public boolean hasRole(String roleCode) {
        return Objects.equals(authenticatedUser().getRoleCode(), roleCode);
    }

    /**
     * Throws when the logged-in user does not have the given role.
     */
    public void requireRole(String roleCode) {
        if (!hasRole(roleCode)) {
            throw new IllegalStateException("USER HAS LESS PRIVILEGES");
        }
    }

    public Boolean isAuthenticated() {
        return jetSecurityContext.isAuthenticated();
    }

    public void requiresAuth() {
        if (Boolean.FALSE.equals(isAuthenticated())) {
            throw new IllegalArgumentException("AUTHENTICATION REQUIRED");
        }
    }

    public SystemUserModel authenticatedUser() {
        if (Boolean.TRUE.equals(isAuthenticated())) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Object principal = authentication.getPrincipal();
            if (principal instanceof SystemUserModel user) {
                return user;
            }
            throw new IllegalStateException("Unexpected principal type: " + principal.getClass().getName());
        }
        throw new IllegalArgumentException("AUTHENTICATION REQUIRED");
    }
}
