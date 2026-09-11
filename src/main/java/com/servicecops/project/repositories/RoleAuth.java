package com.servicecops.project.repositories;

import com.servicecops.project.models.jpahelpers.enums.AppDomains;

import java.util.List;

/**
 * Cached role metadata + permission codes for a single {@code roleCode}.
 */
public record RoleAuth(
        String roleName,
        AppDomains domain,
        List<String> permissionCodes
) {
    public RoleAuth {
        permissionCodes = permissionCodes == null ? List.of() : List.copyOf(permissionCodes);
    }
}
