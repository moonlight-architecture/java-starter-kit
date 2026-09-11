package com.servicecops.project.models.views;

import com.servicecops.project.models.database.SystemUserModel;

/**
 * Safe user payload for API responses — never includes the password hash.
 */
public record UserView(
        Long id,
        String username,
        String firstName,
        String lastName,
        String roleCode,
        String domain
) {
    public static UserView from(SystemUserModel user) {
        return new UserView(
                user.getId(),
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getRoleCode(),
                user.getRoleDomain() == null ? null : user.getRoleDomain().name()
        );
    }
}
