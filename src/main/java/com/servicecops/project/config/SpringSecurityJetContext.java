package com.servicecops.project.config;

import com.jet.moonlight.security.JetSecurityContext;
import com.servicecops.project.models.database.SystemUserModel;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SpringSecurityJetContext implements JetSecurityContext {

    @Override
    public boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }

    @Override
    public boolean hasPermission(String permission) {
        if (!isAuthenticated()) {
            return false;
        }
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(permission));
    }

    @Override
    public boolean hasAnyPermission(String... permissions) {
        if (!isAuthenticated()) {
            return false;
        }
        var authorities = SecurityContextHolder.getContext().getAuthentication().getAuthorities();
        for (String permission : permissions) {
            if (authorities.stream().anyMatch(a -> a.getAuthority().equals(permission))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasRole(String role) {
        SystemUserModel user = currentUser();
        return user != null && role != null && role.equals(user.getRoleCode());
    }

    @Override
    public boolean hasDomain(String domain) {
        SystemUserModel user = currentUser();
        return user != null
                && user.getRoleDomain() != null
                && domain != null
                && domain.equals(user.getRoleDomain().name());
    }

    private SystemUserModel currentUser() {
        if (!isAuthenticated()) {
            return null;
        }
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return principal instanceof SystemUserModel user ? user : null;
    }
}
